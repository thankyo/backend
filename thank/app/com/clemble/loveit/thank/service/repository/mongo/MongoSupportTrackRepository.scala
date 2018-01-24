package com.clemble.loveit.thank.service.repository.mongo

import javax.inject.{Inject, Named}

import com.clemble.loveit.common.model.{Tag, UserID}
import com.clemble.loveit.common.mongo.MongoSafeUtils
import com.clemble.loveit.thank.model.SupportedProject
import com.clemble.loveit.thank.service.repository.{SupportTrackRepository, SupportedProjectRepository}
import play.api.libs.json.{JsObject, Json}

import reactivemongo.play.json._
import reactivemongo.play.json.collection.JSONCollection

import scala.concurrent.{ExecutionContext, Future}

case class MongoSupportTrackRepository @Inject()(
                                             @Named("userSupported") collection: JSONCollection,
                                             implicit val ec: ExecutionContext
                                           ) extends SupportTrackRepository {

  override def markSupported(supporter: UserID, project: SupportedProject): Future[Boolean] = {
    val selector = Json.obj("_id" -> supporter)
    val update = Json.obj("$addToSet" -> Json.obj("supported" -> project))
    MongoSafeUtils.safeSingleUpdate(collection.update(selector, update, upsert = true))
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
