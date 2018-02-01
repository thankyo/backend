package com.clemble.loveit.thank.service.repository.mongo

import javax.inject.{Inject, Named}

import akka.stream.Materializer
import com.clemble.loveit.common.model.{Resource, Tag, UserID}
import com.clemble.loveit.common.mongo.MongoSafeUtils
import com.clemble.loveit.thank.model.SupportedProject
import com.clemble.loveit.thank.model.SupportedProject._
import com.clemble.loveit.thank.service.repository.SupportedProjectRepository
import play.api.libs.json.Json
import reactivemongo.api.indexes.{Index, IndexType}
import reactivemongo.play.json._
import reactivemongo.play.json.collection.JSONCollection

import scala.concurrent.{ExecutionContext, Future}

case class MongoSupportedProjectRepository @Inject()(
                                                 @Named("projects") collection: JSONCollection,
                                                 implicit val ec: ExecutionContext,
                                                 implicit val mat: Materializer
                                               ) extends SupportedProjectRepository {

  MongoSupportedProjectRepository.ensureMeta(collection)

  override def saveProject(project: SupportedProject): Future[Boolean] = {
    findProject(project.resource) flatMap(_ match {
      case Some(_) => {
        val selector = Json.obj("resource" -> project.resource)
        val update = Json.obj("$set" -> project)
        MongoSafeUtils.safeSingleUpdate(collection.update(selector, update))
      }
      case None => {
        MongoSafeUtils.safeSingleUpdate(collection.insert(project))
      }
    })
  }

  override def findProjectsByUser(owner: UserID): Future[List[SupportedProject]] = {
    val selector = Json.obj("user" -> owner)
    MongoSafeUtils.collectAll[SupportedProject](collection, selector)
  }

  override def assignTags(resource: Resource, tags: Set[Tag]): Future[Boolean] = {
    val selector = Json.obj("resource" -> resource)
    val update = Json.obj("$set" -> Json.obj("tags" -> tags))
    MongoSafeUtils.safeSingleUpdate(collection.update(selector, update))
  }

  override def findProject(res: Resource): Future[Option[SupportedProject]] = {
    val query = Json.obj("resource" -> Json.obj("$in" -> res.parents()))
    collection.find(query).one[SupportedProject]
  }

}


object MongoSupportedProjectRepository {

  def ensureMeta(collection: JSONCollection)(implicit ec: ExecutionContext, m: Materializer): Unit = {
    ensureIndexes(collection)
  }

  private def ensureIndexes(collection: JSONCollection)(implicit ec: ExecutionContext): Unit = {
    collection.indexesManager.drop("user_owns_unique")
    MongoSafeUtils.ensureIndexes(
      collection,
      Index(
        key = Seq("resource.uri" -> IndexType.Ascending, "resource.type" -> IndexType.Ascending),
        name = Some("user_owns_unique_resource"),
        unique = true,
        sparse = true
      )
    )
  }

}