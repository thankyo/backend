package com.clemble.loveit.payment.service.repository.mongo

import javax.inject.{Inject, Named, Singleton}

import akka.stream.Materializer
import akka.stream.scaladsl.Source
import com.clemble.loveit.common.error.PaymentException
import com.clemble.loveit.common.model.{Amount, UserID}
import com.clemble.loveit.common.mongo.MongoSafeUtils
import com.clemble.loveit.payment.model.{ChargeAccount, Money, PayoutAccount, UserPayment}
import com.clemble.loveit.payment.service.repository.PaymentRepository
import play.api.libs.json.{JsObject, Json}
import reactivemongo.api.indexes.{Index, IndexType}
import reactivemongo.play.json._
import reactivemongo.play.json.collection.JSONCollection

import scala.concurrent.{ExecutionContext, Future}

@Singleton
case class MongoPaymentRepository @Inject()(
                                             @Named("userPayment") collection: JSONCollection,
                                             implicit val ec: ExecutionContext,
                                             implicit val m: Materializer
                                           ) extends PaymentRepository {

  MongoPaymentRepository.ensureMeta(collection)

  override def save(userPayment: UserPayment): Future[Boolean] = {
    val saveRes = collection.insert(userPayment)
    MongoSafeUtils.safeSingleUpdate(saveRes)
  }

  override def findById(id: UserID): Future[Option[UserPayment]] = {
    val selector = Json.obj("_id" -> id)
    collection.
      find(selector).
      one[UserPayment]
  }

  override def find(): Source[UserPayment, _] = {
    val selector = Json.obj()
    MongoSafeUtils.findAll[UserPayment](collection, selector)
  }

  override def getBalance(user: UserID): Future[Amount] = {
    val selector = Json.obj("_id" -> user)
    val projection = Json.obj("balance" -> 1)
    collection.
      find(selector, projection).
      one[JsObject].
      map(_.flatMap(json => (json \ "balance").asOpt[Amount]).getOrElse(0))
  }

  override def updateBalance(user: UserID, update: Amount): Future[Boolean] = {
    val query = Json.obj("_id" -> user)
    val change = Json.obj("$inc" -> Json.obj("balance" -> update))
    MongoSafeUtils.safeSingleUpdate(collection.update(query, change))
  }

  override def getMonthlyLimit(user: UserID): Future[Option[Money]] = {
    val selector = Json.obj("_id" -> user)
    val projection = Json.obj("monthlyLimit" -> 1)
    val fLimit = collection.find(selector, projection).one[JsObject].map(_.flatMap(json => (json \ "monthlyLimit").asOpt[Money]))
    MongoSafeUtils.safe(fLimit)
  }

  override def setMonthlyLimit(user: UserID, monthlyLimit: Money): Future[Boolean] = {
    if (monthlyLimit.isNegative) throw PaymentException.limitIsNegative(user, monthlyLimit)
    val selector = Json.obj("_id" -> user)
    val update = Json.obj("$set" -> Json.obj("monthlyLimit" -> monthlyLimit))
    MongoSafeUtils.safeSingleUpdate(collection.update(selector, update))
  }

  override def getChargeAccount(user: UserID): Future[Option[ChargeAccount]] = {
    val query = Json.obj("_id" -> user)
    val projection = Json.obj("chargeAccount" -> 1)
    val find = collection.find(query, projection).one[JsObject].map(_.flatMap(json => (json \ "chargeAccount").asOpt[ChargeAccount]))
    MongoSafeUtils.safe(find)
  }

  override def setChargeAccount(user: UserID, chAcc: ChargeAccount): Future[Boolean] = {
    val query = Json.obj("_id" -> user)
    val change = Json.obj("$set" -> Json.obj("chargeAccount" -> chAcc))
    val update = collection.update(query, change).map(res => res.ok && res.n == 1)
    MongoSafeUtils.safe(update)
  }

  override def getPayoutAccount(user: UserID): Future[Option[PayoutAccount]] = {
    val query = Json.obj("_id" -> user)
    val projection = Json.obj("payoutAccount" -> 1)
    val find = collection.find(query, projection).one[JsObject].map(_.flatMap(json => (json \ "payoutAccount").asOpt[PayoutAccount]))
    MongoSafeUtils.safe(find)
  }

  override def setPayoutAccount(user: UserID, ptAcc: PayoutAccount): Future[Boolean] = {
    val query = Json.obj("_id" -> user)
    val change = Json.obj("$set" -> Json.obj("payoutAccount" -> ptAcc))
    val update = collection.update(query, change).map(res => res.ok && res.n == 1)
    MongoSafeUtils.safe(update)
  }

}

object MongoPaymentRepository {

  def ensureMeta(collection: JSONCollection)(implicit ec: ExecutionContext, m: Materializer): Unit = {
    ensureIndexes(collection)
  }

  private def ensureIndexes(collection: JSONCollection)(implicit ec: ExecutionContext): Unit = {
    MongoSafeUtils.ensureIndexes(
      collection,
      Index(
        key = Seq("payoutAccount.accountId" -> IndexType.Ascending),
        name = Some("payout_account_uniquer"),
        unique = true,
        sparse = true
      ),
      Index(
        key = Seq("chargeAccount.customer" -> IndexType.Ascending),
        name = Some("charge_customer_uniquer"),
        unique = true,
        sparse = true
      )
    )
  }

}
