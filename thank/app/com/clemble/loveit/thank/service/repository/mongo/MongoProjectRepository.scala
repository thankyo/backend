package com.clemble.loveit.thank.service.repository.mongo

import javax.inject.{Inject, Named}

import akka.stream.Materializer
import com.clemble.loveit.common.model.{ProjectID, Resource, Tag, UserID}
import com.clemble.loveit.common.model._
import com.clemble.loveit.common.mongo.MongoSafeUtils
import com.clemble.loveit.thank.model.Project
import com.clemble.loveit.thank.model.Project._
import com.clemble.loveit.thank.service.repository.ProjectRepository
import play.api.libs.json.Json
import reactivemongo.api.indexes.{Index, IndexType}
import reactivemongo.play.json._
import reactivemongo.play.json.collection.JSONCollection

import scala.concurrent.{ExecutionContext, Future}

case class MongoProjectRepository @Inject()(
                                                 @Named("projects") collection: JSONCollection,
                                                 implicit val ec: ExecutionContext,
                                                 implicit val mat: Materializer
                                               ) extends ProjectRepository {

  MongoProjectRepository.ensureMeta(collection)

  override def findById(project: ProjectID): Future[Option[Project]] = {
    val selector = Json.obj("_id" -> project)
    collection.find(selector).one[Project]
  }

  override def findAll(ids: List[ProjectID]): Future[List[Project]] = {
    val selector = Json.obj("_id" -> Json.obj("$in" -> ids))
    MongoSafeUtils.collectAll[Project](collection, selector)
  }

  override def saveProject(project: Project): Future[Boolean] = {
    val selector = Json.obj("resource" -> project.resource)
    collection.find(selector).one[Project] flatMap(_ match {
      case Some(existingPrj) => update(project.copy(_id = existingPrj._id))
      case None => MongoSafeUtils.safeSingleUpdate(collection.insert(project))
    })
  }

  override def update(project: Project): Future[Boolean] = {
    val selector = Json.obj("_id" -> project._id)
    val update = Json.obj("$set" -> (Json.toJsObject(project) - "_id"))
    MongoSafeUtils.safeSingleUpdate(collection.update(selector, update))
  }

  override def findProjectsByUser(owner: UserID): Future[List[Project]] = {
    val selector = Json.obj("user" -> owner)
    MongoSafeUtils.collectAll[Project](collection, selector)
  }

  override def assignTags(resource: Resource, tags: Set[Tag]): Future[Boolean] = {
    val selector = Json.obj("resource" -> resource)
    val update = Json.obj("$set" -> Json.obj("tags" -> tags))
    MongoSafeUtils.safeSingleUpdate(collection.update(selector, update))
  }

  override def findProject(res: Resource): Future[Option[Project]] = {
    val query = Json.obj("resource" -> Json.obj("$in" -> res.parents()))
    collection.find(query).one[Project]
  }

}


object MongoProjectRepository {

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