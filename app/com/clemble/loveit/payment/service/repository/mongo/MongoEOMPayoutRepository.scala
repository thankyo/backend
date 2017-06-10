package com.clemble.loveit.payment.service.repository.mongo

import javax.inject.{Inject, Named, Singleton}

import akka.stream.Materializer
import com.clemble.loveit.common.mongo.{MongoSafeUtils, MongoUserAwareRepository}
import com.clemble.loveit.payment.model.{EOMCharge, EOMPayout}
import com.clemble.loveit.payment.service.repository.EOMPayoutRepository
import reactivemongo.api.indexes.{Index, IndexType}
import reactivemongo.play.json.collection.JSONCollection

import scala.concurrent.{ExecutionContext, Future}

@Singleton
case class MongoEOMPayoutRepository @Inject()(@Named("eomPayout") collection: JSONCollection, implicit val ec: ExecutionContext, implicit val m: Materializer) extends EOMPayoutRepository with MongoUserAwareRepository[EOMPayout] {

  override implicit val format = EOMPayout.jsonFormat

  MongoEOMPayoutRepository.ensureMeta(collection)

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
