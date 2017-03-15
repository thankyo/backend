package com.clemble.thank.service.repository.mongo

import com.clemble.thank.model.{Resource, Thank}
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

  override def save(thank: Thank): Future[Boolean] = {
    val withParents = thank.withParents().map(t => {
      Json.toJson(t).as[JsObject]
    })
    val fInsert = collection.bulkInsert(withParents.toStream, false, api.commands.WriteConcern.Acknowledged)
    MongoSafeUtils.safe(fInsert.map(_ => true))
  }

  override def findByResource(resource: Resource): Future[Option[Thank]] = {
    val fSearchResult = collection.find(Json.obj("resource" -> resource)).one[Thank]
    MongoSafeUtils.safe(fSearchResult)
  }

  override def increase(resource: Resource): Future[Boolean] = {
    val query = Json.obj("_id" ->
      Json.obj("$in" ->
        JsArray(URIUtils.toParents(resource).map(Json.toJson(_))
        )
      )
    )
    val increase = Json.obj("$inc" -> Json.obj("given" -> 1))
    val fIncrease = collection.update(
      query,
      increase,
      multi = true
    )
    MongoSafeUtils.safe(fIncrease.map(_ => true))
  }

}
