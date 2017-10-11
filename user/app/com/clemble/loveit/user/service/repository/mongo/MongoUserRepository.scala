package com.clemble.loveit.user.service.repository.mongo

import akka.stream.Materializer
import com.clemble.loveit.user.model._
import com.clemble.loveit.common.model.{UserID}
import com.clemble.loveit.common.mongo.MongoSafeUtils
import com.clemble.loveit.user.service.repository.UserRepository
import javax.inject.{Inject, Named, Singleton}

import akka.stream.scaladsl.Source
import com.mohiva.play.silhouette.api.LoginInfo
import play.api.libs.json._
import reactivemongo.play.json._
import reactivemongo.play.json.collection.JSONCollection

import scala.concurrent.{ExecutionContext, Future}
import reactivemongo.api.indexes.{Index, IndexType}


@Singleton
case class MongoUserRepository @Inject()(
                                          @Named("user") collection: JSONCollection,
                                          implicit val m: Materializer,
                                          implicit val ec: ExecutionContext
                                        ) extends UserRepository {

  MongoUserRepository.ensureMeta(collection)

  override def save(user: User): Future[User] = {
    val userJson = Json.toJson[User](user).as[JsObject] + ("_id" -> JsString(user.id))
    val fInsert = collection.insert(userJson)
    MongoSafeUtils.safe(user, fInsert)
  }

  override def update(user: User): Future[User] = {
    val selector = Json.obj("_id" -> JsString(user.id))
    val update = Json.toJson(user).as[JsObject]
    val fUpdate = collection.update(selector, update)
    MongoSafeUtils.safe(user, fUpdate)
  }

  override def findById(id: UserID): Future[Option[User]] = {
    val query = Json.obj("_id" -> id)
    val fUser = collection.find(query).one[User]
    MongoSafeUtils.safe(fUser)
  }

  override def retrieve(loginInfo: LoginInfo): Future[Option[User]] = {
    val query = Json.obj("profiles.providerID" -> loginInfo.providerID, "profiles.providerKey" -> loginInfo.providerKey)
    val fUser = collection.find(query).one[User]
    MongoSafeUtils.safe(fUser)
  }

  override def remove(users: Seq[UserID]): Future[Boolean] = {
    val query = Json.obj("_id" -> Json.obj("$in" -> JsArray(users.map(JsString))))
    val fRemove = collection.remove(query).map(_.ok)
    MongoSafeUtils.safe(fRemove)
  }

  override def find(): Source[User, _] = {
    MongoSafeUtils.findAll[User](collection, Json.obj())
  }

  override def count(): Future[Int] = {
    collection.count()
  }

}

object MongoUserRepository {

  def ensureMeta(collection: JSONCollection)(implicit ec: ExecutionContext, m: Materializer): Unit = {
    ensureIndexes(collection)
    ensureUpToDate(collection)
  }

  private def ensureIndexes(collection: JSONCollection)(implicit ec: ExecutionContext): Unit = {
    MongoSafeUtils.ensureIndexes(
      collection,
      Index(
        key = Seq("profiles.providerID" -> IndexType.Ascending, "profiles.providerKey" -> IndexType.Ascending),
        name = Some("user_profiles")
      )
    )
  }

  private def ensureUpToDate(collection: JSONCollection)(implicit ec: ExecutionContext, m: Materializer): Unit = {
  }

}
