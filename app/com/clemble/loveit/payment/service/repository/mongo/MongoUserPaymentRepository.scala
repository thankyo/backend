package com.clemble.loveit.payment.service.repository.mongo

import javax.inject.{Inject, Named}

import akka.stream.Materializer
import akka.stream.scaladsl.Source
import com.clemble.loveit.common.model.UserID
import com.clemble.loveit.payment.model.UserPayment
import com.clemble.loveit.payment.service.repository.UserPaymentRepository
import play.api.libs.json.Json
import reactivemongo.api.ReadPreference
import reactivemongo.akkastream.cursorProducer
import reactivemongo.play.json.collection.JSONCollection
import reactivemongo.play.json._

import scala.concurrent.{ExecutionContext, Future}

/**
  * Created by mavarazy on 6/7/17.
  */
case class MongoUserPaymentRepository @Inject()(@Named("user") collection: JSONCollection, implicit val m: Materializer, implicit val ec: ExecutionContext) extends UserPaymentRepository {

  val PROJECTION = Json.obj("id" -> 1, "balance" -> 1, "chargeAccount" -> 1, "payoutAccount" -> 1, "monthlyLimit" -> 1, "pending" -> 1)

  override def findById(id: UserID): Future[Option[UserPayment]] = {
    val selector = Json.obj("_id" -> id)
    collection.
      find(selector, PROJECTION).
      one[UserPayment]
  }

  override def find(): Source[UserPayment, _] = {
    val selector = Json.obj()
    collection.
      find(selector, PROJECTION).
      cursor[UserPayment](ReadPreference.nearest).
      documentSource()
  }

}
