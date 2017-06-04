package com.clemble.loveit.thank.service.repository.mongo

import javax.inject.{Inject, Named, Singleton}

import akka.stream.Materializer
import com.clemble.loveit.common.model.UserID
import com.clemble.loveit.thank.model.{UserResource}
import com.clemble.loveit.thank.service.repository.{UserResourceRepository}
import play.api.libs.json.Json
import reactivemongo.play.json.collection.JSONCollection
import play.modules.reactivemongo.json._
import scala.concurrent.{ExecutionContext, Future}


@Singleton
case class MongoUserResourceRepository @Inject()(@Named("user") collection: JSONCollection,
                                                implicit val m: Materializer,
                                                implicit val ec: ExecutionContext
                                               ) extends UserResourceRepository {

  override def find(user: UserID): Future[Option[UserResource]] = {
    val query = Json.obj("_id" -> user)
    val projection = Json.obj("id" -> 1, "owns" -> 1, "pending" -> 1)
    collection.find(query, projection).one[UserResource]
  }

}
