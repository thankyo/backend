package com.clemble.loveit.payment.service.repository.mongo

import akka.stream.Materializer
import com.clemble.loveit.common.mongo.MongoSafeUtils
import com.clemble.loveit.payment.service.repository.PendingTransactionRepository
import javax.inject.{Inject, Named, Singleton}

import akka.stream.scaladsl.Source
import com.clemble.loveit.common.model.{UserID}
import com.clemble.loveit.payment.model.PendingTransaction
import play.api.libs.json.{JsObject, Json}
import reactivemongo.play.json.collection.JSONCollection

import scala.concurrent.{ExecutionContext, Future}
import reactivemongo.play.json._

@Singleton
case class MongoPendingTransactionRepository @Inject()(
                                                      @Named("userPayment") collection: JSONCollection,
                                                      implicit val m: Materializer,
                                                      implicit val ec: ExecutionContext)
  extends PendingTransactionRepository {

  override def save(user: UserID, transaction: PendingTransaction): Future[Boolean] = {
    val giverSelector = Json.obj("_id" -> user, "charges.resource" -> Json.obj("$ne" -> transaction.resource))
    val giverUpdate = Json.obj("$push" -> Json.obj("charges" -> transaction))
    val updateGiver = MongoSafeUtils.safeSingleUpdate(collection.update(giverSelector, giverUpdate))

    updateGiver.flatMap({
      case true =>
        val ownerSelector = Json.obj("_id" -> transaction.user)
        val ownerUpdate = Json.obj("$push" -> Json.obj("payouts" -> transaction))
        MongoSafeUtils.safeSingleUpdate(collection.update(ownerSelector, ownerUpdate))
      case false =>
        Future.successful(false)
    })
  }

  override def findChargesByUser(user: UserID): Future[List[PendingTransaction]] = {
    val selector = Json.obj("_id" -> user)
    val projection = Json.obj("charges" -> 1)
    collection.find(selector, projection).
      one[JsObject].
      map(_.flatMap(obj => (obj \ "charges").asOpt[List[PendingTransaction]]).getOrElse(List.empty))
  }

  override def findPayoutsByUser(user: UserID): Future[List[PendingTransaction]] = {
    val selector = Json.obj("_id" -> user)
    val projection = Json.obj("payouts" -> 1)
    collection.find(selector, projection).
      one[JsObject].
      map(_.flatMap(obj => (obj \ "payouts").asOpt[List[PendingTransaction]]).getOrElse(List.empty))
  }

  override def findUsersWithPayouts(): Future[List[UserID]] = {
    val selector = Json.obj("payouts" -> Json.obj("$not" -> Json.obj("$size" -> 0)))
    val projection = Json.obj("_id" -> 1)
    MongoSafeUtils.collectAll[JsObject](collection, selector, projection).map(_.map(obj => (obj \ "_id").as[String]))
  }

  override def findUsersWithoutCharges(): Future[List[UserID]] = {
    val selector = Json.obj("charges" -> Json.obj("$size" -> 0))
    val projection = Json.obj("_id" -> 1)
    MongoSafeUtils.collectAll[JsObject](collection, selector, projection).map(_.map(obj => (obj \ "_id").as[String]))
  }

  override def removeCharges(user: UserID, thanks: Seq[PendingTransaction]): Future[Boolean] = {
    val selector = Json.obj("_id" -> user)
    val update = Json.obj("$pull" -> Json.obj("charges" -> Json.obj("$in" -> thanks)))
    MongoSafeUtils.safe(collection.update(selector, update, multi = true).map(_.ok))
  }

  override def removePayouts(user: UserID, transactions: Seq[PendingTransaction]): Future[Boolean] = {
    val selector = Json.obj("_id" -> user)
    val update = Json.obj("$pull" -> Json.obj("payouts" -> Json.obj("$in" -> transactions)))
    MongoSafeUtils.safe(collection.update(selector, update, multi = true).map(_.ok))
  }

}
