package com.clemble.loveit.payment.service.repository.mongo

import akka.stream.Materializer
import com.clemble.loveit.common.mongo.{MongoSafeUtils, MongoUserAwareRepository}
import com.clemble.loveit.payment.model.EOMCharge
import com.clemble.loveit.payment.service.repository.EOMChargeRepository
import javax.inject.{Inject, Named, Singleton}

import akka.stream.scaladsl.Source
import com.clemble.loveit.common.model.UserID
import com.clemble.loveit.payment.model.ChargeStatus.ChargeStatus
import org.joda.time.DateTime
import play.api.libs.json.{JsObject, JsString, JsValue, Json}
import reactivemongo.play.json.collection.JSONCollection
import reactivemongo.play.json._

import scala.concurrent.{ExecutionContext, Future}

@Singleton
case class MongoEOMChargeRepository @Inject()(
                                                         @Named("paymentTransactions") collection: JSONCollection,
                                                         implicit val m: Materializer,
                                                         implicit val ec: ExecutionContext
                                                  ) extends EOMChargeRepository with MongoUserAwareRepository[EOMCharge] {

  override implicit val format = EOMCharge.jsonFormat

  override def listPending(date: DateTime): Source[EOMCharge, _] = ???

  override def updatePending(user: UserID, date: DateTime, status: ChargeStatus, details: JsValue): Future[Boolean] = ???

  override def save(charge: EOMCharge): Future[EOMCharge] = {
    val json = Json.toJson(charge).as[JsObject]
    MongoSafeUtils.safe(charge, collection.insert(json))
  }

}
