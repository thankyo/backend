package com.clemble.loveit.payment.service.repository.mongo

import javax.inject.{Inject, Named, Singleton}

import akka.stream.scaladsl.Source
import com.clemble.loveit.common.model.{Amount, UserID}
import com.clemble.loveit.common.mongo.MongoSafeUtils
import com.clemble.loveit.payment.model.BankDetails
import com.clemble.loveit.payment.service.repository.PaymentRepository
import play.api.libs.json.{JsObject, Json}
import reactivemongo.play.json.collection.JSONCollection
import reactivemongo.play.json._

import scala.concurrent.{ExecutionContext, Future}

@Singleton
case class MongoPaymentRepository @Inject()(@Named("user") collection: JSONCollection, implicit val ec: ExecutionContext) extends PaymentRepository {

  override def listBankDetails(): Source[(String, Option[BankDetails]), _] = {
    Source.empty
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
