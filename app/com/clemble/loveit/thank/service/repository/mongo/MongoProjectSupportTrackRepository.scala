package com.clemble.loveit.thank.service.repository.mongo

import javax.inject.{Inject, Named}
import com.clemble.loveit.common.model.{Project, ProjectID, Tag, UserID}
import com.clemble.loveit.common.mongo.MongoSafeUtils
import com.clemble.loveit.thank.service.repository.ProjectSupportTrackRepository
import play.api.libs.json.{JsObject, Json}
import reactivemongo.play.json._
import reactivemongo.play.json.collection.JSONCollection

import scala.concurrent.{ExecutionContext, Future}

case class MongoProjectSupportTrackRepository @Inject()(
                                             @Named("userSupported") collection: JSONCollection,
                                             implicit val ec: ExecutionContext
                                           ) extends ProjectSupportTrackRepository {

  override def markSupportedBy(supporter: UserID, project: Project): Future[Boolean] = {
    val selector = Json.obj("_id" -> supporter)
    val update = Json.obj("$addToSet" -> Json.obj("supported" -> project._id))
    MongoSafeUtils.safeSingleUpdate(collection.update(selector, update, upsert = true))
  }

  override def getSupported(supporter: UserID): Future[List[ProjectID]] = {
    val selector = Json.obj("_id" -> supporter)
    val projection = Json.obj("supported" -> 1)
    MongoSafeUtils.safe(
      collection.
        find(selector, projection).
        one[JsObject].
        map(_.flatMap(json => (json \ "supported").asOpt[List[ProjectID]]).getOrElse(List.empty[ProjectID])))
  }

}
