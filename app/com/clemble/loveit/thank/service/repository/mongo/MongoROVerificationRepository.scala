package com.clemble.loveit.thank.service.repository.mongo

import javax.inject.{Inject, Named, Singleton}

import akka.stream.Materializer
import com.clemble.loveit.common.model.{Resource, UserID}
import com.clemble.loveit.common.mongo.MongoSafeUtils
import com.clemble.loveit.thank.model.{ROVerification, VerificationStatus}
import com.clemble.loveit.thank.service.repository.ROVerificationRepository
import play.api.libs.json.{JsObject, Json}
import play.modules.reactivemongo.json._
import reactivemongo.api.indexes.{Index, IndexType}
import reactivemongo.play.json.collection.JSONCollection

import scala.concurrent.{ExecutionContext, Future}

@Singleton
case class MongoROVerificationRepository @Inject()(@Named("user") collection: JSONCollection,
                                                   implicit val m: Materializer,
                                                   implicit val ec: ExecutionContext
                                          ) extends ROVerificationRepository {

  MongoROVerificationRepository.ensureMeta(collection)

  override def get(requester: UserID, res: Resource): Future[Option[ROVerification[Resource]]] = {
    list(requester).map(_.find(_.resource == res))
  }

  override def list(user: UserID): Future[Set[ROVerification[Resource]]] = {
    val selector = Json.obj("_id" -> user)
    val projections = Json.obj("ownRequests" -> 1)
    collection.
      find(selector, projections).
      one[JsObject].
      map(_.flatMap(obj => (obj \ "ownRequests").asOpt[Set[ROVerification[Resource]]]).getOrElse(Set.empty))
  }

  override def save(req: ROVerification[Resource]): Future[ROVerification[Resource]] = {
    val selector = Json.obj("_id" -> req.requester, "ownRequests.resource" -> Json.obj("$ne" -> req.resource))
    // TODO no point using $addToSet verificationCode will be different each time
    val push = Json.obj("$push" -> Json.obj("ownRequests" -> req))
    MongoSafeUtils.safe(req, collection.update(selector, push))
  }

  override def update(user: UserID, res: Resource, status: VerificationStatus): Future[Boolean] = {
    val selector = Json.obj("_id" -> user, "ownRequests.resource" -> res)
    val updateStatus = Json.obj("$set" -> Json.obj("ownRequests.$.status" -> status))
    MongoSafeUtils.safe(collection.update(selector, updateStatus).map(res => res.ok && res.n == 1))
  }

  override def delete(requester: UserID, res: Resource): Future[Boolean] = {
    val selector = Json.obj("_id" -> requester)
    val push = Json.obj("$pull" -> Json.obj("ownRequests" -> Json.obj("resource" -> res)))
    MongoSafeUtils.safe(collection.update(selector, push).map(res => res.ok && res.n == 1))
  }

}

object MongoROVerificationRepository {

  def ensureMeta(collection: JSONCollection)(implicit ec: ExecutionContext, m: Materializer) = {
    MongoSafeUtils.ensureIndexes(
      collection,
      Index(
        key = Seq("ownRequests.resource.uri" -> IndexType.Ascending, "ownRequests.resource.type" -> IndexType.Ascending),
        name = Some("verification_resource_unique"),
        unique = true,
        sparse = true
      )
    )
  }

}