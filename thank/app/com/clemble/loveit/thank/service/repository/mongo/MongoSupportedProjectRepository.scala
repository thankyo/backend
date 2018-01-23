package com.clemble.loveit.thank.service.repository.mongo

import javax.inject.{Inject, Named}

import com.clemble.loveit.common.model.{Tag, UserID}
import com.clemble.loveit.common.mongo.MongoSafeUtils
import com.clemble.loveit.thank.model.SupportedProject
import com.clemble.loveit.thank.service.repository.SupportedProjectRepository
import play.api.libs.json.{JsObject, Json}
import reactivemongo.play.json._
import reactivemongo.play.json.collection.JSONCollection

import scala.concurrent.{ExecutionContext, Future}

class MongoSupportedProjectRepository @Inject()(
                                                 @Named("userSupported") collection: JSONCollection,
                                                 @Named("userResource") resCollection: JSONCollection,
                                                 implicit val ec: ExecutionContext
                                               ) extends SupportedProjectRepository {


  override def getProject(owner: UserID): Future[Option[SupportedProject]] = {
    val selector = Json.obj("_id" -> owner)
    val projection = Json.obj("project" -> 1)
    resCollection.find(selector, projection).one[JsObject].map(optJson => {
      optJson.flatMap(json => (json \ "project").asOpt[SupportedProject])
    })
  }

  override def markSupported(supporter: UserID, project: SupportedProject): Future[Boolean] = {
    val selector = Json.obj("_id" -> supporter)
    val update = Json.obj("$addToSet" -> Json.obj("supported" -> project))
    MongoSafeUtils.safeSingleUpdate(collection.update(selector, update, upsert = true))
  }

  override def setTags(user: UserID, tags: Set[Tag]): Future[Boolean] = {
    val selector = Json.obj("_id" -> user)
    val update = Json.obj("$set" -> tags)
    MongoSafeUtils.safeSingleUpdate(collection.update(selector, update))
  }

  override def getSupported(supporter: UserID): Future[List[SupportedProject]] = {
    val selector = Json.obj("_id" -> supporter)
    val projection = Json.obj("supported" -> 1)
    MongoSafeUtils.safe(
      collection.
        find(selector, projection).
        one[JsObject].
        map(_.flatMap(json => (json \ "supported").asOpt[List[SupportedProject]]).getOrElse(List.empty[SupportedProject])))
  }

}
