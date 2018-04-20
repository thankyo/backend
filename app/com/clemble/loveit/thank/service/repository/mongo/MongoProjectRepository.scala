package com.clemble.loveit.thank.service.repository.mongo

import javax.inject.{Inject, Named}
import akka.stream.Materializer
import com.clemble.loveit.common.model.{Project, ProjectID, Resource, Tag, UserID, _}
import com.clemble.loveit.common.mongo.MongoSafeUtils
import com.clemble.loveit.common.util.IDGenerator
import com.clemble.loveit.common.model.Project._
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

  override def findProjectById(project: ProjectID): Future[Option[Project]] = {
    val selector = Json.obj("_id" -> project)
    collection.find(selector).one[Project]
  }

  override def findAllProjects(ids: List[ProjectID]): Future[List[Project]] = {
    val selector = Json.obj("_id" -> Json.obj("$in" -> ids))
    MongoSafeUtils.collectAll[Project](collection, selector)
  }

  override def saveProject(project: Project): Future[Project] = {
    val fSave = MongoSafeUtils.safeSingleUpdate(collection.insert(project))
    fSave.filter(_ == true).map(_ => project)
  }

  override def updateProject(project: Project): Future[Boolean] = {
    val selector = Json.obj("_id" -> project._id)
    val update = Json.obj("$set" -> (Json.toJsObject(project) - "_id"))
    MongoSafeUtils.safeSingleUpdate(collection.update(selector, update))
  }

  override def findProjectsByUser(owner: UserID): Future[List[Project]] = {
    val selector = Json.obj("user" -> owner)
    MongoSafeUtils.collectAll[Project](collection, selector)
  }

  override def findProjectByUrl(url: Resource): Future[Option[Project]] = {
    val query = Json.obj("url" -> Json.obj("$in" -> url.parents()))
    collection.find(query).one[Project]
  }

  override def deleteProject(id: ProjectID): Future[Boolean] = {
    val selector = Json.obj("_id" -> id)
    MongoSafeUtils.safeSingleUpdate(collection.remove(selector))
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
        key = Seq("url" -> IndexType.Ascending),
        name = Some("user_owns_unique_url"),
        unique = true,
        sparse = true
      )
    )
  }

}