package com.clemble.loveit.payment.service.repository.mongo

import com.clemble.loveit.payment.model._
import java.time.{LocalDateTime, YearMonth}
import javax.inject.{Inject, Named, Singleton}

import akka.stream.Materializer
import com.clemble.loveit.common.mongo.MongoSafeUtils
import com.clemble.loveit.payment.model.EOMStatus
import com.clemble.loveit.payment.service.repository.EOMStatusRepository
import play.api.libs.json.{JsObject, Json}
import reactivemongo.api.indexes.{Index, IndexType}
import reactivemongo.play.json.collection.JSONCollection
import reactivemongo.play.json._

import scala.concurrent.{ExecutionContext, Future}

@Singleton
case class MongoEOMStatusRepository @Inject()(@Named("eomStatus") collection: JSONCollection, implicit val ec: ExecutionContext, implicit val m: Materializer) extends EOMStatusRepository {

  MongoEOMStatusRepository.ensureMeta(collection)

  override def get(yom: YearMonth): Future[Option[EOMStatus]] = {
    val selector = Json.obj("yom" -> yom)
    collection.find(selector).one[EOMStatus]
  }

  override def save(status: EOMStatus): Future[EOMStatus] = {
    MongoSafeUtils.safe(status, collection.insert(Json.toJson(status).as[JsObject]))
  }

  override def update(yom: YearMonth, createCharges: EOMStatistics, applyCharges: EOMStatistics, createPayout: EOMStatistics, applyPayout: EOMStatistics, finished: LocalDateTime): Future[Boolean] = {
    val selector = Json.obj("yom" -> yom)
    val update = Json.obj("$set" -> Json.obj(
      "createCharges" -> createCharges,
      "applyCharges" -> applyCharges,
      "createPayout" -> createPayout,
      "applyPayout" -> applyPayout,
      "finished" -> finished
    ))
    MongoSafeUtils.safeSingleUpdate(collection.update(selector, update))
  }

}

object MongoEOMStatusRepository {

  def ensureMeta(collection: JSONCollection)(implicit ec: ExecutionContext, m: Materializer): Unit = {
    ensureIndexes(collection)
  }

  private def ensureIndexes(collection: JSONCollection)(implicit ec: ExecutionContext): Unit = {
    MongoSafeUtils.ensureIndexes(
      collection,
      Index(
        key = Seq("yom" -> IndexType.Ascending),
        name = Some("yom_uniquer"),
        unique = true
      )
    )
  }
}
