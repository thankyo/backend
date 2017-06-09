package com.clemble.loveit.payment.service.repository.mongo

import akka.stream.Materializer
import com.clemble.loveit.common.mongo.{MongoSafeUtils, MongoUserAwareRepository}
import com.clemble.loveit.payment.model.Charge
import com.clemble.loveit.payment.service.repository.PaymentTransactionRepository
import javax.inject.{Inject, Singleton, Named}
import play.api.libs.json.{JsObject, JsString, Json}
import reactivemongo.play.json.collection.JSONCollection
import reactivemongo.play.json._

import scala.concurrent.{ExecutionContext, Future}

@Singleton
case class MongoPaymentTransactionRepository @Inject() (
                                                         @Named("paymentTransactions") collection: JSONCollection,
                                                         implicit val m: Materializer,
                                                         implicit val ec: ExecutionContext
                                                  ) extends PaymentTransactionRepository with MongoUserAwareRepository[Charge] {

  override implicit val format = Charge.jsonFormat

  override def save(tr: Charge): Future[Charge] = {
    val json = Json.toJson(tr).as[JsObject] + ("_id" -> JsString(tr.id))
    MongoSafeUtils.safe(tr, collection.insert(json))
  }


}
