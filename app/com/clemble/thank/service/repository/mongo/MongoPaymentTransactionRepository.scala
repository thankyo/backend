package com.clemble.thank.service.repository.mongo

import akka.stream.Materializer
import com.clemble.thank.model.{PaymentTransaction, ThankTransaction, UserID}
import com.clemble.thank.service.repository.{PaymentTransactionRepository, ThankTransactionRepository}
import com.google.inject.name.Named
import com.google.inject.{Inject, Singleton}
import play.api.libs.json.{JsObject, JsString, Json}
import reactivemongo.api.ReadPreference
import reactivemongo.play.json._
import reactivemongo.play.json.collection.JSONCollection
import akka.stream.scaladsl.Source
import reactivemongo.akkastream.cursorProducer
import reactivemongo.api.indexes.{Index, IndexType}

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
