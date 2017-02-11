package com.clemble.thank.service.repository.mongo

import com.clemble.thank.model.{Amount, User, UserId}
import com.clemble.thank.service.repository.UserRepository
import com.google.inject.Inject
import com.google.inject.name.Named
import play.api.libs.json.{JsObject, JsString, Json}
import play.modules.reactivemongo.json._
import reactivemongo.play.json.collection.JSONCollection

import scala.concurrent.{ExecutionContext, Future}

case class MongoUserRepository @Inject()(
                                          @Named("user") collection: JSONCollection,
                                          implicit val ec: ExecutionContext
                                        ) extends UserRepository {

  override def save(user: User): Future[User] = {
    val userJson = Json.toJson[User](user).as[JsObject] + ("_id" -> JsString(user.id))
    val fInsert = collection.insert(userJson)
    MongoExceptionUtils.safe(() => user, fInsert)
  }

  override def findById(id: UserId): Future[Option[User]] = {
    val fUser = collection.find(Json.obj("_id" -> id)).one[User]
    MongoExceptionUtils.safe(fUser)
  }

  override def changeBalance(id: UserId, diff: Amount): Future[Boolean] = {
    val query = Json.obj("_id" -> id)
    val change = Json.obj("$inc" -> Json.obj("balance" -> diff))
    MongoExceptionUtils.safe(() => true, collection.update(query, change))
  }

  override def findOwner(uri: String): Future[Option[User]] = {
    val query = Json.obj("owns" -> uri)
    MongoExceptionUtils.safe(collection.find(query).one[User])
  }
}
