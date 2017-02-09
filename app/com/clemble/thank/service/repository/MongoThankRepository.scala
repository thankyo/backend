package com.clemble.thank.service.repository

import com.clemble.thank.model.Thank
import com.google.inject.Inject
import com.google.inject.name.Named
import play.api.libs.iteratee.Enumerator
import play.api.libs.json.Json
import reactivemongo.api.ReadPreference
import reactivemongo.play.json.collection.JSONCollection

import reactivemongo.play.iteratees.cursorProducer
import play.modules.reactivemongo.json._

import scala.concurrent.{ExecutionContext, Future}

case class MongoThankRepository @Inject()(
                                           @Named("thank") collection: JSONCollection,
                                           implicit val ec: ExecutionContext
                                         ) extends ThankRepository {

  override def create(thank: Thank): Future[Thank] = {
    collection.insert(thank).
      filter(_.ok).
      flatMap(_ => findByUrlOrFail(thank.url))
  }

  override def findByUrl(url: String): Future[Option[Thank]] = {
    collection.find(Json.obj("url" -> url)).one[Thank]
  }

  /**
    * Similar to findByUrl, but with a failure if value is missing
    */
  private def findByUrlOrFail(url: String): Future[Thank] = findByUrl(url).map(_.get)

  override def increase(url: String): Future[Thank] = {
    collection.update(
      Json.obj("url" -> url),
      Json.obj("$inc" -> Json.obj("given" -> 1))
    ).
      filter(wrRes => wrRes.ok && wrRes.nModified == 1).
      flatMap(_ => findByUrlOrFail(url))
  }

  override def findAllIncludingSub(url: String): Enumerator[Thank] = {
    collection.
      find(Json.obj("url" -> Json.obj("$regex" -> s"$url.*"))).
      cursor[Thank](ReadPreference.nearest).
      enumerator()
  }

}
