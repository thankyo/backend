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
    val giverSelector = Json.obj("_id" -> user, "pending.resource" -> Json.obj("$ne" -> transaction.resource))
    val giverUpdate = Json.obj("$push" -> Json.obj("pending" -> transaction))
    val updateGiver = MongoSafeUtils.safeSingleUpdate(collection.update(giverSelector, giverUpdate))

    val ownerSelector = Json.obj("_id" -> transaction.user)
    val ownerUpdate = Json.obj("$push" -> Json.obj("incoming" -> transaction))
    val updateOwner = MongoSafeUtils.safeSingleUpdate(collection.update(ownerSelector, ownerUpdate))

    Future.sequence(List(updateGiver, updateOwner)).map(_.forall(_ == true))
  }

  override def findOutgoingByUser(user: UserID): Future[List[PendingTransaction]] = {
    val selector = Json.obj("_id" -> user)
    val projection = Json.obj("pending" -> 1)
    collection.find(selector, projection).
      one[JsObject].
      map(_.flatMap(obj => (obj \ "pending").asOpt[List[PendingTransaction]]).getOrElse(List.empty))
  }

  override def findIncomingByUser(user: UserID): Future[List[PendingTransaction]] = {
    val selector = Json.obj("_id" -> user)
    val projection = Json.obj("incoming" -> 1)
    collection.find(selector, projection).
      one[JsObject].
      map(_.flatMap(obj => (obj \ "incoming").asOpt[List[PendingTransaction]]).getOrElse(List.empty))
  }

  override def findUsersWithIncoming(): Future[List[UserID]] = {
    val selector = Json.obj("incoming" -> Json.obj("$not" -> Json.obj("$size" -> 0)))
    val projection = Json.obj("_id" -> 1)
    MongoSafeUtils.collectAll[JsObject](collection, selector, projection).map(_.map(obj => (obj \ "_id").as[String]))
  }

  override def findUsersWithoutOutgoing(): Future[List[UserID]] = {
    val selector = Json.obj("pending" -> Json.obj("$size" -> 0))
    val projection = Json.obj("_id" -> 1)
    MongoSafeUtils.collectAll[JsObject](collection, selector, projection).map(_.map(obj => (obj \ "_id").as[String]))
  }

  override def removeOutgoing(user: UserID, thanks: Seq[PendingTransaction]): Future[Boolean] = {
    val selector = Json.obj("_id" -> user)
    val update = Json.obj("$pull" -> Json.obj("pending" -> Json.obj("$in" -> thanks)))
    MongoSafeUtils.safe(collection.update(selector, update, multi = true).map(_.ok))
  }

  override def removeIncoming(user: UserID, transactions: Seq[PendingTransaction]): Future[Boolean] = {
    val selector = Json.obj("_id" -> user)
    val update = Json.obj("$pull" -> Json.obj("outgoing" -> Json.obj("$in" -> transactions)))
    MongoSafeUtils.safe(collection.update(selector, update, multi = true).map(_.ok))
  }

}
