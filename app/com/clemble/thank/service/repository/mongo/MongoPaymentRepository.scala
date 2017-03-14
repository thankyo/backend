package com.clemble.thank.service.repository.mongo

import akka.stream.Materializer
import com.clemble.thank.model.{Payment, UserId}
import com.clemble.thank.service.repository.PaymentRepository
import com.google.inject.name.Named
import com.google.inject.{Inject, Singleton}
import play.api.libs.json.Json
import reactivemongo.api.ReadPreference
import reactivemongo.play.json._
import reactivemongo.play.json.collection.JSONCollection
import akka.stream.scaladsl.Source
import reactivemongo.akkastream.{cursorProducer, State}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
case class MongoPaymentRepository @Inject()(
                                             @Named("payment") collection: JSONCollection,
                                             implicit val m: Materializer,
                                             implicit val ec: ExecutionContext) extends PaymentRepository {

  override def findByUser(user: UserId): Source[Payment, _] = {
    collection.find(Json.obj("user" -> user)).cursor[Payment](ReadPreference.nearest).documentSource()
  }

  override def save(payment: Payment): Future[Payment] = {
    collection.insert(payment).filter(_.ok).map(_ => payment)
  }

}
