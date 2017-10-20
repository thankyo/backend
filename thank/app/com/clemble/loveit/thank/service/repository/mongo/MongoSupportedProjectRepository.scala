package com.clemble.loveit.thank.service.repository.mongo

import javax.inject.{Inject, Named}

import com.clemble.loveit.common.model.UserID
import com.clemble.loveit.common.mongo.MongoSafeUtils
import com.clemble.loveit.thank.model.SupportedProject
import com.clemble.loveit.thank.service.repository.SupportedProjectRepo
import com.clemble.loveit.user.model.User
import play.api.libs.json.{JsObject, Json}
import reactivemongo.play.json.collection.JSONCollection
import reactivemongo.play.json._

import scala.concurrent.{ExecutionContext, Future}

class MongoSupportedProjectRepository @Inject()(
                                                @Named("userSupported") collection: JSONCollection,
                                                implicit val ec: ExecutionContext
                                              ) extends SupportedProjectRepo {

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
