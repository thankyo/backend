package com.clemble.loveit.thank.service.repository.mongo

import javax.inject.{Inject, Named, Singleton}

import akka.stream.Materializer
import com.clemble.loveit.common.model.UserID
import com.clemble.loveit.common.mongo.MongoSafeUtils
import com.clemble.loveit.thank.model.{OwnershipVerificationRequest, OwnershipVerificationRequestStatus}
import com.clemble.loveit.thank.service.repository.OwnershipVerificationRepository
import play.api.libs.json.{JsObject, Json}
import play.modules.reactivemongo.json._
import reactivemongo.play.json.collection.JSONCollection

import scala.concurrent.{ExecutionContext, Future}

@Singleton
case class MongoOwnershipVerificationRepository @Inject()(@Named("user") collection: JSONCollection,
                                                     implicit val m: Materializer,
                                                     implicit val ec: ExecutionContext
                                          ) extends OwnershipVerificationRepository {

  override def list(user: UserID): Future[Set[OwnershipVerificationRequest]] = {
    val selector = Json.obj("_id" -> user)
    val projections = Json.obj("ownRequests" -> 1)
    collection.
      find(selector, projections).
      one[JsObject].
      map(_.flatMap(obj => (obj \ "ownRequests").asOpt[Set[OwnershipVerificationRequest]]).getOrElse(Set.empty))
  }

  override def save(req: OwnershipVerificationRequest): Future[OwnershipVerificationRequest] = {
    val selector = Json.obj("_id" -> req.requester)
    val push = Json.obj("$push" -> Json.obj("ownRequests" -> req))
    MongoSafeUtils.safe(req, collection.update(selector, push))
  }

  override def update(req: OwnershipVerificationRequest, status: OwnershipVerificationRequestStatus): Future[Boolean] = {
    val selector = Json.obj("_id" -> req.requester, "ownRequests.resource" -> req.resource)
    val updateStatus = Json.obj("$set" -> Json.obj("ownRequests.$.status" -> status))
    MongoSafeUtils.safe(collection.update(selector, updateStatus).map(res => res.ok && res.n == 1))
  }

}
