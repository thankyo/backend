package com.clemble.loveit.payment.service.repository.mongo

import javax.inject.{Inject, Named, Singleton}

import akka.stream.Materializer
import com.clemble.loveit.common.error.PaymentException
import com.clemble.loveit.common.model.{Amount, UserID}
import com.clemble.loveit.common.mongo.MongoSafeUtils
import com.clemble.loveit.payment.model.{BankDetails, Money}
import com.clemble.loveit.payment.service.repository.PaymentRepository
import play.api.libs.json.{JsObject, Json}
import reactivemongo.api.indexes.{Index, IndexType}
import reactivemongo.bson.{BSONDocument, BSONString}
import reactivemongo.play.json._
import reactivemongo.play.json.collection.JSONCollection

import scala.concurrent.{ExecutionContext, Future}

@Singleton
case class MongoPaymentRepository @Inject()(
                                             @Named("user") collection: JSONCollection,
                                             implicit val ec: ExecutionContext,
                                             implicit val m: Materializer
                                           ) extends PaymentRepository {

  MongoPaymentRepository.ensureMeta(collection)

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

  override def getBankDetails(user: UserID): Future[Option[BankDetails]] = {
    val query = Json.obj("_id" -> user)
    val projection = Json.obj("bankDetails" -> 1)
    val find = collection.find(query, projection).one[JsObject].map(_.flatMap(json => (json \ "bankDetails").asOpt[BankDetails]))
    MongoSafeUtils.safe(find)
  }

  override def setBankDetails(user: UserID, bankDetails: BankDetails): Future[Boolean] = {
    val query = Json.obj("_id" -> user)
    val change = Json.obj("$set" -> Json.obj("bankDetails" -> bankDetails))
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
        key = Seq("bankDetails.type" -> IndexType.Ascending, "bankDetails.customer" -> IndexType.Ascending),
        name = Some("stripe_customer_uniquer"),
        unique = true,
        partialFilter = Some(BSONDocument("bankDetails.type" -> BSONString("stripe")))
      ),
      Index(
        key = Seq("bankDetails.type" -> IndexType.Ascending, "bankDetails.email" -> IndexType.Ascending),
        name = Some("paypal_email_unique"),
        unique = true,
        partialFilter = Some(BSONDocument("bankDetails.type" -> BSONString("payPal")))
      )

    )
  }

}
