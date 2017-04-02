package com.clemble.loveit.common.mongo

import akka.stream.Materializer
import akka.stream.scaladsl.Source
import com.clemble.loveit.common.model.UserID
import com.clemble.loveit.user.model.UserAware
import com.clemble.loveit.user.service.repository.UserAwareRepository
import play.api.libs.json.{Format, Json}
import reactivemongo.akkastream.cursorProducer
import reactivemongo.api.ReadPreference
import reactivemongo.api.indexes.{Index, IndexType}
import reactivemongo.play.json._
import reactivemongo.play.json.collection.JSONCollection

import scala.concurrent.ExecutionContext

trait MongoUserAwareRepository[T <: UserAware] extends UserAwareRepository[T] {

  val collection: JSONCollection
  implicit val m: Materializer
  implicit val ec: ExecutionContext
  implicit val format: Format[T]

  MongoSafeUtils.ensureIndexes(
    collection,
    Index(
      key = Seq("user" -> IndexType.Ascending, "created" -> IndexType.Ascending),
      name = Some("user_created_asc")
    ),
    Index(
      key = Seq("user" -> IndexType.Ascending),
      name = Some("user_asc")
    )
  )

  override def findByUser(user: UserID): Source[T, _] = {
    collection.find(Json.obj("user" -> user)).sort(Json.obj("created" -> 1)).cursor[T](ReadPreference.nearest).documentSource()
  }


}
