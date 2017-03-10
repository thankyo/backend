package com.clemble.thank.service.repository.mongo

import com.clemble.thank.model.{Amount, ResourceOwnership, User, UserId}
import com.clemble.thank.service.repository.UserRepository
import com.google.inject.Inject
import com.google.inject.name.Named
import play.api.libs.json.{JsArray, JsObject, JsString, Json}
import reactivemongo.api.Cursor.ContOnError
import reactivemongo.api.ReadPreference
import reactivemongo.play.json.collection.JSONCollection

import scala.concurrent.{ExecutionContext, Future}
import play.modules.reactivemongo.json._

case class MongoUserRepository @Inject()(
                                          @Named("user") collection: JSONCollection,
                                          implicit val ec: ExecutionContext
                                        ) extends UserRepository {

  override def save(user: User): Future[User] = {
    val userJson = Json.toJson[User](user).as[JsObject] + ("_id" -> JsString(user.id))
    val fInsert = collection.insert(userJson)
    MongoSafeUtils.safe(() => user, fInsert)
  }

  override def findById(id: UserId): Future[Option[User]] = {
    val fUser = collection.find(Json.obj("_id" -> id)).one[User]
    MongoSafeUtils.safe(fUser)
  }

  override def changeBalance(id: UserId, diff: Amount): Future[Boolean] = {
    val query = Json.obj("_id" -> id)
    val change = Json.obj("$inc" -> Json.obj("balance" -> diff))
    MongoSafeUtils.safe(() => true, collection.update(query, change))
  }

  override def findOwners(uris: List[ResourceOwnership]): Future[List[User]] = {
    val query = Json.obj("owns" -> Json.obj(
      "$in" -> JsArray(uris.map(Json.toJson(_)))
    ))
    MongoSafeUtils.safe(
      collection.
        find(query).
        cursor[User](ReadPreference.nearest).
        collect[List](Int.MaxValue, ContOnError[List[User]]())
    )
  }

}
