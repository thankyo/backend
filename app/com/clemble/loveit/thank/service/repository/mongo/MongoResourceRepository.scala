package com.clemble.loveit.thank.service.repository.mongo

import javax.inject.{Inject, Named}

import akka.stream.Materializer
import com.clemble.loveit.common.error.{RepositoryException}
import com.clemble.loveit.common.model.{Resource, UserID}
import com.clemble.loveit.common.mongo.MongoSafeUtils
import com.clemble.loveit.thank.service.repository.ResourceRepository
import play.api.libs.json.{JsObject, Json}
import reactivemongo.api.indexes.{Index, IndexType}
import reactivemongo.play.json.collection.JSONCollection
import reactivemongo.play.json._

import scala.concurrent.{ExecutionContext, Future}

case class MongoResourceRepository @Inject() (@Named("user") collection: JSONCollection, implicit val m: Materializer, implicit val ec: ExecutionContext) extends ResourceRepository{

  MongoResourceRepository.ensureMeta(collection)

  override def findOwner(res: Resource): Future[Option[String]] = {
    val query = Json.obj("owns" -> res)
    val projection = Json.obj("_id" -> 1)
    collection.find(query, projection).one[JsObject].flatMap(_ match {
      case Some(owner) => Future.successful((owner \ "_id").asOpt[String])
      case None => res.parent match {
        case Some(parRes) => findOwner(parRes)
        case None => Future.successful(None)
      }
    })
  }

  override def listOwned(user: UserID): Future[Set[Resource]] = {
    val query = Json.obj("_id" -> user)
    val projection = Json.obj("owns" -> 1)
    val fQuery = collection.find(query, projection).one[JsObject].map(_ match {
      case Some(resources) => (resources \ "owns").as[Set[Resource]]
      case None => Set.empty[Resource]
    })
    MongoSafeUtils.safe(fQuery)
  }

  override def assignOwnership(user: UserID, resource: Resource): Future[Boolean] = {
    def cleanPreviousOwner(resource: Resource): Future[Boolean] = {
      for {
        userOpt <- findOwner(resource)
        user = userOpt.get
        removed <- MongoSafeUtils.safeSingleUpdate(collection.update(Json.obj("_id" -> user), Json.obj("$pull" -> Json.obj("owns" -> resource))))
      } yield {
        removed
      }
    }

    def doAssignOwnership(): Future[Boolean] = {
      val query = Json.obj("_id" -> user)
      val update = Json.obj("$addToSet" -> Json.obj("owns" -> resource))
      MongoSafeUtils.safeSingleUpdate(collection.update(query, update, multi = false))
    }

    doAssignOwnership().
      recoverWith[Boolean]({
        case RepositoryException(RepositoryException.DUPLICATE_KEY_CODE, _) =>
          for {
            cleaned <- cleanPreviousOwner(resource)
            retry <- doAssignOwnership() if (cleaned)
          } yield {
            retry
          }
      })
  }

}

object MongoResourceRepository {

  def ensureMeta(collection: JSONCollection)(implicit ec: ExecutionContext, m: Materializer): Unit = {
    ensureIndexes(collection)
  }

  private def ensureIndexes(collection: JSONCollection)(implicit ec: ExecutionContext): Unit = {
    collection.indexesManager.drop("user_owns_unique")
    MongoSafeUtils.ensureIndexes(
      collection,
      Index(
        key = Seq("owns.uri" -> IndexType.Ascending, "owns.type" -> IndexType.Ascending),
        name = Some("user_owns_unique_resource"),
        unique = true,
        sparse = true
      )
    )
  }

}
