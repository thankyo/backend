package com.clemble.thank.service.repository

import com.clemble.thank.model.{User, UserId}
import com.google.inject.Inject
import com.google.inject.name.Named
import play.api.libs.iteratee.Enumerator
import play.api.libs.json.Json
import reactivemongo.api.ReadPreference
import reactivemongo.play.json.collection.JSONCollection
import reactivemongo.play.iteratees.cursorProducer
import play.modules.reactivemongo.json._

import scala.concurrent.{ExecutionContext, Future}

case class MongoUserRepository @Inject()(
                                          @Named("user") collection: JSONCollection,
                                          implicit val ec: ExecutionContext
                                        ) extends UserRepository {

  override def findAll(): Enumerator[User] = {
    collection.find(Json.obj()).cursor[User](ReadPreference.nearest).enumerator()
  }

  override def findById(id: UserId): Future[Option[User]] = {
    collection.find(Json.obj("_id" -> id)).one[User]
  }

  override def create(user: User): Future[User] = {
    collection.insert(user).filter(_.ok).map(_ => user)
  }

}
