package com.clemble.thank.service.repository.mongo

import com.clemble.thank.model.Thank
import com.clemble.thank.service.repository.ThankRepository
import com.google.inject.name.Named
import com.google.inject.{Inject, Singleton}
import play.api.libs.iteratee.Enumerator
import play.api.libs.json.{JsObject, JsString, Json}
import play.modules.reactivemongo.json._
import reactivemongo.api.ReadPreference
import reactivemongo.play.iteratees.cursorProducer
import reactivemongo.play.json.collection.JSONCollection

import scala.concurrent.{ExecutionContext, Future}

@Singleton
case class MongoThankRepository @Inject()(
                                           @Named("thank") collection: JSONCollection,
                                           implicit val ec: ExecutionContext
                                         ) extends ThankRepository {

  override def create(thank: Thank): Future[Thank] = {
    val thankJson = Json.toJson(thank).as[JsObject] + ("_id" -> JsString(thank.uri))
    val fThank = collection.insert(thankJson)
    MongoExceptionUtils.safe(() => thank, fThank)
  }

  override def findByUrl(url: String): Future[Option[Thank]] = {
    collection.find(Json.obj("url" -> url)).one[Thank]
  }

  override def increase(url: String): Future[Boolean] = {
    val query = Json.obj("url" -> Json.obj("$regex" -> s"$url.*"))
    collection.update(
      query,
      Json.obj("$inc" -> Json.obj("given" -> 1))
    ).map(wrRes => wrRes.ok)
  }

}
