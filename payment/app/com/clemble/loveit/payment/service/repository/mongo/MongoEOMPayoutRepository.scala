package com.clemble.loveit.payment.service.repository.mongo

import java.time.YearMonth
import javax.inject.{Inject, Named, Singleton}

import akka.stream.Materializer
import akka.stream.scaladsl.Source
import com.clemble.loveit.common.model.UserID
import com.clemble.loveit.common.mongo.{MongoSafeUtils, MongoUserAwareRepository}
import com.clemble.loveit.payment.model.PayoutStatus.PayoutStatus
import com.clemble.loveit.payment.model.{ChargeStatus, EOMCharge, EOMPayout, PayoutStatus}
import com.clemble.loveit.payment.service.repository.EOMPayoutRepository
import play.api.libs.json.{JsObject, JsValue, Json, OFormat}
import reactivemongo.api.ReadPreference
import reactivemongo.api.indexes.{Index, IndexType}
import reactivemongo.play.json.collection.JSONCollection
import com.clemble.loveit.payment.model._
import reactivemongo.play.json._
import reactivemongo.akkastream.cursorProducer

import scala.concurrent.{ExecutionContext, Future}

@Singleton
case class MongoEOMPayoutRepository @Inject()(@Named("eomPayout") collection: JSONCollection, implicit val ec: ExecutionContext, implicit val m: Materializer) extends EOMPayoutRepository with MongoUserAwareRepository[EOMPayout] {

  override implicit val format: OFormat[EOMPayout] = EOMPayout.jsonFormat

  MongoEOMPayoutRepository.ensureMeta(collection)

  override def listCreated(yom: YearMonth): Future[List[UserID]] = {
    val selector = Json.obj("yom" -> yom)
    val projection = Json.obj("user" -> 1)
    MongoSafeUtils.collectAll[JsObject](collection, selector, projection).map(_.map(obj => (obj \ "user").as[String]))
  }

  override def listPending(yom: YearMonth): Source[EOMPayout, _] = {
    val selector = Json.obj("yom" -> yom, "status" -> PayoutStatus.Pending)
    MongoSafeUtils.findAll[EOMPayout](collection, selector)
  }

  override def updatePending(user: UserID, yom: YearMonth, status: PayoutStatus, details: JsValue): Future[Boolean] = {
    val selector = Json.obj("user" -> user, "yom" -> yom, "status" -> PayoutStatus.Pending)
    val update = Json.obj("$set" -> Json.obj("status" -> status, "details" -> details))
    MongoSafeUtils.safeSingleUpdate(collection.update(selector, update))
  }

  override def save(payout: EOMPayout): Future[Boolean] = {
    MongoSafeUtils.safeSingleUpdate(collection.insert[EOMPayout](payout))
  }

}

object MongoEOMPayoutRepository {

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
