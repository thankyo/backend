package com.clemble.thank.service.repository.mongo

import com.clemble.thank.model.{Payment, UserId}
import com.clemble.thank.service.repository.PaymentRepository
import com.google.inject.name.Named
import com.google.inject.{Inject, Singleton}
import play.api.libs.iteratee.Enumerator
import play.api.libs.json.Json
import reactivemongo.api.ReadPreference
import reactivemongo.play.iteratees.cursorProducer
import reactivemongo.play.json._
import reactivemongo.play.json.collection.JSONCollection

import scala.concurrent.{ExecutionContext, Future}

@Singleton
case class MongoPaymentRepository @Inject()(@Named("payment") collection: JSONCollection, implicit val ec: ExecutionContext) extends PaymentRepository {

  override def findByUser(user: UserId): Enumerator[Payment] = {
    collection.find(Json.obj("user" -> user)).cursor[Payment](ReadPreference.nearest).enumerator()
  }

  override def save(payment: Payment): Future[Payment] = {
    collection.insert(payment).filter(_.ok).map(_ => payment)
  }

}
