package com.clemble.loveit.thank.service.repository.mongo

import akka.stream.Materializer
import com.clemble.loveit.common.error.{FieldValidationError, RepositoryException}
import com.clemble.loveit.common.model.Project._
import com.clemble.loveit.common.model._
import com.clemble.loveit.common.model.{OwnedProject, Project, ProjectID, Resource, UserID}
import com.clemble.loveit.common.mongo.MongoSafeUtils
import com.clemble.loveit.common.mongo.MongoSafeUtils.toException
import com.clemble.loveit.thank.model.UserProjects
import com.clemble.loveit.thank.service.repository.UserProjectsRepository
import javax.inject.{Inject, Named, Singleton}
import play.api.libs.json.{JsObject, JsString, Json}
import reactivemongo.api.indexes.{Index, IndexType}
import reactivemongo.core.errors.DatabaseException
import reactivemongo.play.json._
import reactivemongo.play.json.collection.JSONCollection
import reactivemongo.play.json.commands.DefaultJSONCommandError

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class MongoUserProjectsRepository @Inject() (
  @Named("userProject") collection: JSONCollection,
  implicit val m: Materializer,
  implicit val ec: ExecutionContext
) extends UserProjectsRepository {

  MongoUserProjectsRepository.ensureMeta(collection)

  val INSTALLED_PROJECTION = Json.obj("installed" -> 1)

  override def findProjectById(id: ProjectID): Future[Option[Project]] = {
    val selector = Json.obj("installed._id" -> id)
    collection.find(selector).one[UserProjects].map(_.flatMap(_.installed.find(p => p._id == id)))
  }

  override def findProjectByUrl(url: Resource): Future[Option[Project]] = {
    val selector = Json.obj("installed.url" -> Json.obj("$in" -> url.parents()))
    collection
      .find(selector)
      .one[UserProjects]
      .map(_.flatMap(_.installed.find(p => url.startsWith(p.url))))
  }

  override def findProjectsByUser(user: UserID): Future[List[Project]] = {
    collection.find(Json.obj("_id" -> user), Json.obj("installed" -> 1))
      .one[JsObject]
      .map(_.flatMap(json => (json \ "installed").asOpt[List[Project]]).getOrElse(List.empty))
  }

  override def findAllProjects(ids: List[ProjectID]): Future[List[Project]] = {
    val selector = Json.obj("installed._id" -> Json.obj("$in" -> ids))
    MongoSafeUtils
      .collectAll[UserProjects](collection, selector)
      .map(_.flatMap(_.installed.filter(project => ids.contains(project._id))))
  }

  override def updateProject(project: Project): Future[Boolean] = {
    val selector = Json.obj("_id" -> project.user, "installed._id" -> project._id, "installed.url" -> project.url)
    val update = Json.obj("$set" -> Json.obj("installed.$" -> project))
    MongoSafeUtils.safeSingleUpdate(collection.update(selector, update)).recoverWith(errorHandler)
  }

  override def deleteProject(user: UserID, id: ProjectID): Future[Boolean] = {
    val selector = Json.obj("_id" -> user)
    val deleteQuery = Json.obj("$pull" -> Json.obj("installed" -> Json.obj("_id" -> id)))
    MongoSafeUtils.safeSingleUpdate(collection.update(selector, deleteQuery))
  }

  override def findById(user: UserID): Future[Option[UserProjects]] = {
    val selector = Json.obj("_id" -> user)
    collection.find(selector).one[UserProjects]
  }

  override def save(projects: UserProjects): Future[UserProjects] = {
    val presentation = Json.toJsObject(projects) + ("_id" -> JsString(projects.user))
    MongoSafeUtils.safeSingleUpdate(collection.insert(presentation)).map({
      case true => projects
      case false => throw new IllegalArgumentException(s"Could not update ${projects.user}")
    }).recoverWith(errorHandler)
  }

  override def saveOwnedProject(user: UserID, owned: Seq[OwnedProject]): Future[UserProjects] = {
    val selector = Json.obj("_id" -> user)
    collection
      .update(selector, Json.obj("$pull" -> Json.obj("owned" -> Json.obj("url" -> Json.obj("$in" -> owned.map(_.url))))))
      .flatMap(_ => {
        val update = Json.obj("$addToSet" -> Json.obj("owned" -> Json.obj("$each" -> owned)))
        collection.findAndUpdate(selector, update, true)
          .map(_.result[UserProjects].get)
      }).recoverWith(errorHandler)
  }

  override def deleteOwnedProject(user: UserID, url: String): Future[UserProjects] = {
    val selector = Json.obj("_id" -> user)
    val update = Json.obj("$pull" -> Json.obj("owned" -> Json.obj("url" -> url)))
    collection.findAndUpdate(selector, update, fetchNewObject = true).map(_.result[UserProjects].get)
  }

  override def saveProject(project: Project): Future[Project] = {
    val selector = Json.obj("_id" -> project.user,
      "owned.url" -> project.url,
      "installed.url" -> Json.obj("$ne" -> project.url)
    )
    val update = Json.obj("$addToSet" -> Json.obj("installed" -> project))
    MongoSafeUtils.safeSingleUpdate(collection.update(selector, update)).map({
      case true => project
      case false => throw new IllegalArgumentException(s"Could not create project")
    }).recoverWith(errorHandler[Project])
  }

  private def errorHandler[T]: PartialFunction[Throwable, Future[T]]  = {
    case dbExc: DatabaseException if (dbExc.code == Some(11000) && dbExc.message.contains("user_project_owned_unique_url")) =>
      Future.failed(FieldValidationError("url", "Already owned"))
    case err: DefaultJSONCommandError if (err.code == Some(11000) && err.errmsg.exists(_.contains("user_project_owned_unique_url"))) =>
      Future.failed(FieldValidationError("url", "Already owned"))
    case RepositoryException(RepositoryException.DUPLICATE_KEY_CODE, msg) if msg.contains("user_project_owned_unique_url") =>
      Future.failed(FieldValidationError("url", "Already owned"))
  }

}


object MongoUserProjectsRepository {

  def ensureMeta(collection: JSONCollection)(implicit ec: ExecutionContext, m: Materializer): Unit = {
    MongoSafeUtils.ensureIndexes(
      collection,
      Index(
        name = Some("user_project_installed_unique_url"),
        key = Seq("installed.url" -> IndexType.Ascending),
        unique = true,
        sparse = true,
        background = false
      ),
      Index(
        name = Some("user_project_owned_unique_url"),
        key = Seq("owned.url" -> IndexType.Ascending),
        unique = true,
        sparse = true,
        background = false
      )
    )
  }

}