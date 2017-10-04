package com.clemble.loveit.payment.service.repository.mongo

import com.clemble.loveit.payment.model._
import java.time.{LocalDateTime, YearMonth}
import javax.inject.{Inject, Named, Singleton}

import akka.stream.Materializer
import com.clemble.loveit.common.mongo.MongoSafeUtils
import com.clemble.loveit.payment.model.EOMStatus
import com.clemble.loveit.payment.service.repository.EOMStatusRepository
import play.api.libs.json.{Json, Writes}
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
    MongoSafeUtils.safe(status, collection.insert(status))
  }

  override def updateCreateCharges(yom: YearMonth, createCharges: EOMStatistics) = {
    updateField(yom, "createCharges", createCharges)
  }

  override def updateApplyCharges(yom: YearMonth, applyCharges: EOMStatistics) = {
    updateField(yom, "applyCharges", applyCharges)
  }

  override def updateCreatePayout(yom: YearMonth, createPayout: EOMStatistics) = {
    updateField(yom, "createPayout", createPayout)
  }

  override def updateApplyPayout(yom: YearMonth, applyPayout: EOMStatistics) = {
    updateField(yom, "applyPayout", applyPayout)
  }

  override def updateFinished(yom: YearMonth, finished: LocalDateTime) = {
    updateField(yom, "finished", finished)
  }

  private def updateField[T](yom: YearMonth, field: String, value: T)(implicit writes: Writes[T]) = {
    val selector = Json.obj("yom" -> yom)
    val update = Json.obj("$set" -> Json.obj(
      field -> value
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
