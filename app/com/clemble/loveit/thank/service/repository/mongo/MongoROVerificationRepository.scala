package com.clemble.loveit.thank.service.repository.mongo

import javax.inject.{Inject, Named, Singleton}

import akka.stream.Materializer
import com.clemble.loveit.common.model.{Resource, UserID}
import com.clemble.loveit.common.mongo.MongoSafeUtils
import com.clemble.loveit.thank.model.{ROVerificationRequest, ROVerificationRequestStatus, VerificationID}
import com.clemble.loveit.thank.service.repository.ROVerificationRepository
import play.api.libs.json.{JsObject, Json}
import play.modules.reactivemongo.json._
import reactivemongo.play.json.collection.JSONCollection

import scala.concurrent.{ExecutionContext, Future}

@Singleton
case class MongoROVerificationRepository @Inject()(@Named("user") collection: JSONCollection,
                                                   implicit val m: Materializer,
                                                   implicit val ec: ExecutionContext
                                          ) extends ROVerificationRepository {

  override def get(requester: UserID, request: VerificationID): Future[Option[ROVerificationRequest[Resource]]] = {
    list(requester).map(_.find(_.id == request))
  }

  override def list(user: UserID): Future[Set[ROVerificationRequest[Resource]]] = {
    val selector = Json.obj("_id" -> user)
    val projections = Json.obj("ownRequests" -> 1)
    collection.
      find(selector, projections).
      one[JsObject].
      map(_.flatMap(obj => (obj \ "ownRequests").asOpt[Set[ROVerificationRequest[Resource]]]).getOrElse(Set.empty))
  }

  override def save(req: ROVerificationRequest[Resource]): Future[ROVerificationRequest[Resource]] = {
    val selector = Json.obj("_id" -> req.requester)
    val push = Json.obj("$push" -> Json.obj("ownRequests" -> req))
    MongoSafeUtils.safe(req, collection.update(selector, push))
  }

  override def update(req: ROVerificationRequest[Resource], status: ROVerificationRequestStatus): Future[Boolean] = {
    val selector = Json.obj("_id" -> req.requester, "ownRequests.resource" -> req.resource)
    val updateStatus = Json.obj("$set" -> Json.obj("ownRequests.$.status" -> status))
    MongoSafeUtils.safe(collection.update(selector, updateStatus).map(res => res.ok && res.n == 1))
  }

  override def delete(requester: UserID, request: VerificationID): Future[Boolean] = {
    val selector = Json.obj("_id" -> requester)
    val push = Json.obj("$pull" -> Json.obj("ownRequests" -> Json.obj("id" -> request)))
    MongoSafeUtils.safe(collection.update(selector, push).map(res => res.ok && res.n == 1))
  }

}
