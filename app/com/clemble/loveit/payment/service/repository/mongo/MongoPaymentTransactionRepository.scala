package com.clemble.loveit.payment.service.repository.mongo

import akka.stream.Materializer
import com.clemble.loveit.common.mongo.{MongoSafeUtils, MongoUserAwareRepository}
import com.clemble.loveit.payment.model.PaymentTransaction
import com.clemble.loveit.payment.service.repository.PaymentTransactionRepository
import com.google.inject.Inject
import com.google.inject.name.Named
import play.api.libs.json.{JsObject, JsString, Json}
import reactivemongo.play.json.collection.JSONCollection
import reactivemongo.play.json._

import scala.concurrent.{ExecutionContext, Future}

case class MongoPaymentTransactionRepository @Inject() (
                                                         @Named("paymentTransactions") collection: JSONCollection,
                                                         implicit val m: Materializer,
                                                         implicit val ec: ExecutionContext
                                                  ) extends PaymentTransactionRepository with MongoUserAwareRepository[PaymentTransaction] {

  override implicit val format = PaymentTransaction.jsonFormat

  override def save(tr: PaymentTransaction): Future[PaymentTransaction] = {
    val json = Json.toJson(tr).as[JsObject] + ("_id" -> JsString(tr.id))
    MongoSafeUtils.safe(() => tr, collection.insert(json))
  }


}
