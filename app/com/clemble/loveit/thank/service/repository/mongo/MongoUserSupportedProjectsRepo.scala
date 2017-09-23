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

class MongoUserSupportedProjectsRepo @Inject()(
                                                @Named("user") collection: JSONCollection,
                                                implicit val ec: ExecutionContext
                                              ) extends UserSupportedProjectsRepo {

  override def getRef(project: UserID): Future[Option[User]] = {
    val selector = Json.obj("_id" -> project)
    val projection = Json.obj("id" -> 1, "firstName" -> 1, "lastName" -> 1, "thumbnail" -> 1, "dateOfBirth" -> 1)
    MongoSafeUtils.safe(collection.find(selector, projection).one[User])
  }

  override def markSupported(supporter: UserID, project: User): Future[Boolean] = {
    val selector = Json.obj("_id" -> supporter)
    val update = Json.obj("$addToSet" -> Json.obj("supported" -> project))
    MongoSafeUtils.safeSingleUpdate(collection.update(selector, update))
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
