package com.clemble.loveit.thank.service.repository.mongo

import javax.inject.{Inject, Named, Singleton}

import akka.stream.Materializer
import com.clemble.loveit.common.model.{Resource, UserID}
import com.clemble.loveit.common.mongo.MongoSafeUtils
import com.clemble.loveit.thank.model.{SupportedProject, UserResource}
import com.clemble.loveit.thank.service.repository.UserResourceRepository
import play.api.libs.json.{JsObject, Json}
import reactivemongo.play.json._
import reactivemongo.play.json.collection.JSONCollection

import scala.concurrent.{ExecutionContext, Future}


@Singleton
case class MongoUserResourceRepository @Inject()(@Named("userResource") collection: JSONCollection,
                                                implicit val m: Materializer,
                                                implicit val ec: ExecutionContext
                                               ) extends UserResourceRepository {

  override def save(uRes: UserResource): Future[Boolean] = {
    val saveReq = collection.insert(uRes)
    MongoSafeUtils.safeSingleUpdate(saveReq)
  }

  override def find(user: UserID): Future[Option[UserResource]] = {
    val query = Json.obj("_id" -> user)
    collection.find(query).one[UserResource]
  }

  override def findOwner(res: Resource): Future[Option[SupportedProject]] = {
    val query = Json.obj("owns" -> Json.obj("$in" -> res.parents()))
    collection.find(query, Json.obj("project" -> 1))
      .one[JsObject]
      .map(_.flatMap(json => (json \ "project").asOpt[SupportedProject]))
  }

}
