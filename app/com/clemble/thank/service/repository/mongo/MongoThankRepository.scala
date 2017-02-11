package com.clemble.thank.service.repository.mongo

import com.clemble.thank.model.Thank
import com.clemble.thank.service.repository.ThankRepository
import com.clemble.thank.util.URIUtils
import com.google.inject.name.Named
import com.google.inject.{Inject, Singleton}
import play.api.libs.json.{JsArray, JsObject, JsString, Json}
import play.modules.reactivemongo.json._
import reactivemongo.api
import reactivemongo.play.json.collection.JSONCollection

import scala.concurrent.{ExecutionContext, Future}

@Singleton
case class MongoThankRepository @Inject()(
                                           @Named("thank") collection: JSONCollection,
                                           implicit val ec: ExecutionContext
                                         ) extends ThankRepository {

  override def create(thank: Thank): Future[Boolean] = {
    val withParents = thank.withParents().map(t => {
      Json.toJson(t).as[JsObject] + ("_id" -> JsString(t.uri))
    })
    val fInsert = collection.bulkInsert(withParents.toStream, false, api.commands.WriteConcern.Acknowledged)
    MongoExceptionUtils.safe(fInsert.map(_ => true))
  }

  override def findByURI(uri: String): Future[Option[Thank]] = {
    collection.find(Json.obj("_id" -> uri)).one[Thank]
  }

  override def increase(uri: String): Future[Boolean] = {
    val query = Json.obj("_id" ->
      Json.obj("$in" ->
        JsArray(URIUtils.toParents(uri).map(JsString(_))
        )
      )
    )
    val increase = Json.obj("$inc" -> Json.obj("given" -> 1))
    val fIncrease = collection.update(
      query,
      increase,
      multi = true
    )
    MongoExceptionUtils.safe(fIncrease.map(_ => true))
  }

}
