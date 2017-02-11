package com.clemble.thank.service.repository.mongo

import com.clemble.thank.model.{User, UserId}
import com.clemble.thank.service.repository.UserRepository
import com.google.inject.Inject
import com.google.inject.name.Named
import play.api.libs.iteratee.Enumerator
import play.api.libs.json.{JsObject, JsString, Json}
import play.modules.reactivemongo.json._
import reactivemongo.api.ReadPreference
import reactivemongo.play.iteratees.cursorProducer
import reactivemongo.play.json.collection.JSONCollection

import scala.concurrent.{ExecutionContext, Future}

case class MongoUserRepository @Inject()(
                                          @Named("user") collection: JSONCollection,
                                          implicit val ec: ExecutionContext
                                        ) extends UserRepository {

  override def findAll(): Enumerator[User] = {
    collection.find(Json.obj()).cursor[User](ReadPreference.nearest).enumerator()
  }

  override def findById(id: UserId): Future[Option[User]] = {
    val fUser = collection.find(Json.obj("_id" -> id)).one[User]
    MongoExceptionUtils.safe(fUser)
  }

  override def create(user: User): Future[User] = {
    val userJson = Json.toJson[User](user).as[JsObject] + ("_id" -> JsString(user.id))
    val fInsert = collection.insert(userJson)
    MongoExceptionUtils.safe(() => user, fInsert)
  }

}
