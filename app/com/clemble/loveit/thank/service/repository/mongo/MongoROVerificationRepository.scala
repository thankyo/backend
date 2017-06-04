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

  override def get(user: UserID): Future[Option[ROVerification[Resource]]] = {
    val selector = Json.obj("_id" -> user)
    val projections = Json.obj("verification" -> 1)
    collection.
      find(selector, projections).
      one[JsObject].
      map(_.flatMap(obj => (obj \ "verification").asOpt[ROVerification[Resource]]))
  }

  override def save(user: UserID, req: ROVerification[Resource]): Future[ROVerification[Resource]] = {
    val selector = Json.obj("_id" -> user)
    val push = Json.obj("$set" -> Json.obj("verification" -> req))
    MongoSafeUtils.safe(req, collection.update(selector, push))
  }

  override def update(user: UserID, res: Resource, status: VerificationStatus): Future[Boolean] = {
    val selector = Json.obj("_id" -> user, "verification.resource" -> res)
    val updateStatus = Json.obj("$set" -> Json.obj("verification.status" -> status))
    MongoSafeUtils.safe(collection.update(selector, updateStatus).map(res => res.ok && res.n == 1))
  }

  override def delete(user: UserID): Future[Boolean] = {
    val selector = Json.obj("_id" -> user)
    val push = Json.obj("$unset" -> Json.obj("verification" -> ""))
    MongoSafeUtils.safe(collection.update(selector, push).map(res => res.ok && res.n == 1))
  }

}

object MongoROVerificationRepository {

  def ensureMeta(collection: JSONCollection)(implicit ec: ExecutionContext, m: Materializer) = {
    MongoSafeUtils.ensureIndexes(
      collection,
      Index(
        key = Seq("verification.resource.uri" -> IndexType.Ascending, "verification.resource.type" -> IndexType.Ascending),
        name = Some("verification_unique_per_user"),
        unique = true,
        sparse = true
      )
    )
  }

}