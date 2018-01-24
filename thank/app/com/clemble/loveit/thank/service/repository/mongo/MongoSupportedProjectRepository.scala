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
                                                 @Named("userResource") collection: JSONCollection,
                                                 implicit val ec: ExecutionContext
                                               ) extends SupportedProjectRepository {


  override def getProject(owner: UserID): Future[Option[SupportedProject]] = {
    val selector = Json.obj("_id" -> owner)
    val projection = Json.obj("project" -> 1)
    collection.find(selector, projection).one[JsObject].map(optJson => {
      optJson.flatMap(json => (json \ "project").asOpt[SupportedProject])
    })
  }

  override def setTags(user: UserID, tags: Set[Tag]): Future[Boolean] = {
    val selector = Json.obj("_id" -> user)
    val update = Json.obj("$set" -> Json.obj("project.tags" -> tags))
    MongoSafeUtils.safeSingleUpdate(collection.update(selector, update))
  }

}
