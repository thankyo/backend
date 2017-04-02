package com.clemble.loveit.thank.service.repository.mongo

import com.clemble.loveit.common.mongo.MongoSafeUtils
import com.clemble.loveit.common.model.Resource
import com.clemble.loveit.thank.model.Thank
import com.clemble.loveit.thank.service.repository.ThankRepository
import com.google.inject.name.Named
import com.google.inject.{Inject, Singleton}
import play.api.libs.json.{JsArray, JsObject, Json}
import play.modules.reactivemongo.json._
import reactivemongo.api
import reactivemongo.api.indexes.{Index, IndexType}
import reactivemongo.play.json.collection.JSONCollection

import scala.concurrent.{ExecutionContext, Future}

@Singleton
case class MongoThankRepository @Inject()(
                                           @Named("thank") collection: JSONCollection,
                                           implicit val ec: ExecutionContext
                                         ) extends ThankRepository {

  MongoSafeUtils.ensureIndexes(
    collection,
    Index(
      key = Seq("resource.type" -> IndexType.Ascending, "resource.uri" -> IndexType.Ascending),
      name = Some("recourse_is_unique"),
      unique = true
    )
  )

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
    val query = Json.obj("resource" ->
      Json.obj("$in" ->
        JsArray(resource.parents().map(Json.toJson(_))
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
