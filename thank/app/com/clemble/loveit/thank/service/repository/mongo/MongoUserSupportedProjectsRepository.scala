package com.clemble.loveit.thank.service.repository.mongo

import javax.inject.{Inject, Named}

import com.clemble.loveit.common.model.UserID
import com.clemble.loveit.common.mongo.MongoSafeUtils
import com.clemble.loveit.thank.service.repository.UserSupportedProjectsRepo
import com.clemble.loveit.user.model.User
import play.api.libs.json.{JsObject, Json}
import reactivemongo.play.json.collection.JSONCollection
import reactivemongo.play.json._
import scala.concurrent.{ExecutionContext, Future}

class MongoUserSupportedProjectsRepository @Inject()(
                                                @Named("userSupported") collection: JSONCollection,
                                                implicit val ec: ExecutionContext
                                              ) extends UserSupportedProjectsRepo {

  override def markSupported(supporter: UserID, project: User): Future[Boolean] = {
    val selector = Json.obj("_id" -> supporter)
    val update = Json.obj("$addToSet" -> Json.obj("supported" -> project))
    MongoSafeUtils.safeSingleUpdate(collection.update(selector, update, upsert = true))
  }

  override def getSupported(supporter: UserID): Future[List[User]] = {
    val selector = Json.obj("_id" -> supporter)
    val projection = Json.obj("supported" -> 1)
    MongoSafeUtils.safe(
      collection.
        find(selector, projection).
        one[JsObject].
        map(_.flatMap(json => (json \ "supported").asOpt[List[User]]).getOrElse(List.empty[User])))
  }

}
