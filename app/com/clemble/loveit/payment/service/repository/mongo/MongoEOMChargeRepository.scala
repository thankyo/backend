package com.clemble.loveit.payment.service.repository.mongo

import akka.stream.Materializer
import com.clemble.loveit.common.mongo.{MongoSafeUtils, MongoUserAwareRepository}
import com.clemble.loveit.payment.model.{ChargeStatus, EOMCharge, UserPayment}
import com.clemble.loveit.payment.service.repository.EOMChargeRepository
import javax.inject.{Inject, Named, Singleton}

import akka.stream.scaladsl.Source
import com.clemble.loveit.common.model.UserID
import com.clemble.loveit.payment.model.ChargeStatus.ChargeStatus
import org.joda.time.DateTime
import play.api.libs.json.{JsObject, JsValue, Json}
import reactivemongo.api.ReadPreference

import scala.concurrent.{ExecutionContext, Future}
import reactivemongo.akkastream.cursorProducer
import reactivemongo.api.indexes.{Index, IndexType}
import reactivemongo.play.json.collection.JSONCollection
import reactivemongo.play.json._

@Singleton
case class MongoEOMChargeRepository @Inject()(
                                                         @Named("eomCharge") collection: JSONCollection,
                                                         implicit val m: Materializer,
                                                         implicit val ec: ExecutionContext
                                                  ) extends EOMChargeRepository with MongoUserAwareRepository[EOMCharge] {

  MongoEOMChargeRepository.ensureMeta(collection)

  override implicit val format = EOMCharge.jsonFormat

  override def listPending(date: DateTime): Source[EOMCharge, _] = {
    val selector = Json.obj()
    val projection = Json.obj("status" -> ChargeStatus.Pending)
    collection.
      find(selector, projection).
      cursor[EOMCharge](ReadPreference.nearest).
      documentSource()
  }

  override def updatePending(user: UserID, date: DateTime, status: ChargeStatus, details: JsValue): Future[Boolean] = ???

  override def save(charge: EOMCharge): Future[EOMCharge] = {
    val json = Json.toJson(charge).as[JsObject]
    MongoSafeUtils.safe(charge, collection.insert(json))
  }

}

object MongoEOMChargeRepository {

  def ensureMeta(collection: JSONCollection)(implicit ec: ExecutionContext, m: Materializer): Unit = {
    ensureIndexes(collection)
  }

  private def ensureIndexes(collection: JSONCollection)(implicit ec: ExecutionContext): Unit = {
    MongoSafeUtils.ensureIndexes(
      collection,
      Index(
        key = Seq("yom" -> IndexType.Ascending, "user" -> IndexType.Ascending),
        name = Some("yom_user_uniquer"),
        unique = true
      )
    )
  }

}
