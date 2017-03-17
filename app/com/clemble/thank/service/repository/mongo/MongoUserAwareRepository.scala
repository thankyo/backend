package com.clemble.thank.service.repository.mongo

import akka.stream.Materializer
import com.clemble.thank.model.{UserAware, UserID}
import com.clemble.thank.service.repository.UserAwareRepository
import play.api.libs.json.{Format, Json}
import reactivemongo.api.ReadPreference
import reactivemongo.play.json._
import reactivemongo.play.json.collection.JSONCollection
import akka.stream.scaladsl.Source
import reactivemongo.akkastream.cursorProducer
import reactivemongo.api.indexes.{Index, IndexType}

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
