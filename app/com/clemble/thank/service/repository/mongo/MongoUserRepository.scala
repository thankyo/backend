package com.clemble.thank.service.repository.mongo

import com.clemble.thank.model._
import com.clemble.thank.service.repository.UserRepository
import com.google.inject.Inject
import com.google.inject.name.Named
import com.mohiva.play.silhouette.api.LoginInfo
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

  override def update(user: User): Future[User] = {
    val selector = Json.obj("_id" -> JsString(user.id))
    val update = Json.toJson(user).as[JsObject]
    val fUpdate = collection.update(selector, update)
    MongoSafeUtils.safe(() => user, fUpdate)
  }

  override def findById(id: UserID): Future[Option[User]] = {
    val query = Json.obj("_id" -> id)
    val fUser = collection.find(query).one[User]
    MongoSafeUtils.safe(fUser)
  }

  override def retrieve(loginInfo: LoginInfo): Future[Option[UserIdentity]] = {
    val query = Json.obj("profiles.providerID" -> loginInfo.providerID, "profiles.providerKey" -> loginInfo.providerKey)
    val fUser = collection.find(query).one[User].map(_.map(_.toIdentity()))
    MongoSafeUtils.safe(fUser)
  }

  override def changeBalance(id: UserID, diff: Amount): Future[Boolean] = {
    val query = Json.obj("_id" -> id)
    val change = Json.obj("$inc" -> Json.obj("balance" -> diff))
    MongoSafeUtils.safe(() => true, collection.update(query, change))
  }

  override def findRelated(uri: ResourceOwnership): Future[List[User]] = {
    val query = Json.obj("owns.uri" -> Json.obj("$regex" -> s"${uri.resource}.*"))
    doFind(query)
  }

  override def remove(users: Seq[UserID]): Future[Boolean] = {
    val query = Json.obj("_id" -> Json.obj("$in" -> JsArray(users.map(JsString))))
    val fRemove = collection.remove(query).map(_.ok)
    MongoSafeUtils.safe(fRemove)
  }

  override def findOwners(uris: List[ResourceOwnership]): Future[List[User]] = {
    val query = Json.obj("owns" -> Json.obj("$in" -> JsArray(uris.map(Json.toJson(_)))))
    doFind(query)
  }

  private def doFind(query: JsObject): Future[List[User]] = {
    val users = collection.
      find(query).
      cursor[User](ReadPreference.nearest).
      collect[List](Int.MaxValue, ContOnError[List[User]]())
    MongoSafeUtils.safe(users)
  }

}
