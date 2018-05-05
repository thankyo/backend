package com.clemble.loveit.thank.service.repository.mongo

import akka.stream.Materializer
import com.clemble.loveit.common.error.{FieldValidationError, RepositoryException}
import com.clemble.loveit.common.model.Project._
import com.clemble.loveit.common.model._
import com.clemble.loveit.common.model.{OwnedProject, Project, ProjectID, Resource, UserID}
import com.clemble.loveit.common.mongo.MongoSafeUtils
import com.clemble.loveit.thank.model.UserProject
import com.clemble.loveit.thank.service.repository.UserProjectsRepository
import javax.inject.{Inject, Named, Singleton}
import play.api.libs.json._
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
    collection.find(selector).one[UserProject].map(_.flatMap(_.installed.find(p => p._id == id)))
  }

  override def findProjectByUrl(url: Resource): Future[Option[Project]] = {
    val selector = Json.obj("installed.url" -> Json.obj("$in" -> url.parents()))
    collection
      .find(selector)
      .one[UserProject]
      .map(_.flatMap(_.installed.find(p => url.startsWith(p.url))))
  }

  override def findProjectsByUser(user: UserID): Future[List[Project]] = {
    collection.find(Json.obj("_id" -> user), Json.obj("installed" -> 1))
      .one[JsObject]
      .map(_.flatMap(json => (json \ "installed").asOpt[List[Project]]).getOrElse(List.empty))
  }


  override def findAll(): Future[List[UserProject]] = {
    val selector = Json.obj()
    MongoSafeUtils.collectAll[UserProject](collection, selector)
  }

  override def findAllProjects(ids: List[ProjectID]): Future[List[Project]] = {
    val selector = Json.obj("installed._id" -> Json.obj("$in" -> ids))
    MongoSafeUtils
      .collectAll[UserProject](collection, selector)
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

  override def findById(user: UserID): Future[Option[UserProject]] = {
    val selector = Json.obj("_id" -> user)
    collection.find(selector).one[UserProject]
  }

  override def save(projects: UserProject): Future[UserProject] = {
    val presentation = Json.toJsObject(projects) + ("_id" -> JsString(projects.user))
    MongoSafeUtils.safeSingleUpdate(collection.insert(presentation)).map({
      case true => projects
      case false => throw new IllegalArgumentException(s"Could not update ${projects.user}")
    }).recoverWith(errorHandler)
  }

  private def saveOwnedProject[T <: ProjectLike](user: UserID, field: String, owned: Seq[T])(implicit format: OFormat[T]): Future[UserProject] = {
    val selector = Json.obj("_id" -> user)
    collection
      .update(selector, Json.obj("$pull" -> Json.obj(field -> Json.obj("url" -> Json.obj("$in" -> owned.map(_.url))))))
      .flatMap(_ => {
        val update = Json.obj("$addToSet" -> Json.obj(field -> Json.obj("$each" -> owned)))
        collection.findAndUpdate(selector, update, true)
          .map(_.result[UserProject].get)
      }).recoverWith(errorHandler)
  }


  override def saveGoogleProjects(user: UserID, projects: Seq[OwnedProject]): Future[UserProject] = {
    saveOwnedProject(user, "google", projects)
  }

  override def saveTumblrProjects(user: UserID, projects: Seq[OwnedProject]): Future[UserProject] = {
    saveOwnedProject(user, "tumblr", projects)
  }


  override def findDibsProject(user: UserID): Future[Seq[DibsProject]] = {
    val selector = Json.obj("_id" -> user)
    val projection = Json.obj("dibs" -> 1)
    collection.find(selector, projection).one[JsObject].map({
      case Some(jsObj) => (jsObj \ "dibs").asOpt[Seq[DibsProject]].getOrElse(Seq.empty[DibsProject])
      case None => Seq.empty[DibsProject]
    })
  }

  override def saveDibsProjects(user: UserID, projects: Seq[DibsProject]): Future[UserProject] = {
    saveOwnedProject(user, "dibs", projects)
  }

  override def saveEmailProjects(user: UserID, projects: Seq[EmailProject]): Future[UserProject] = {
    saveOwnedProject(user, "email", projects)
  }

  override def validateEmailProject(user: UserID, email: Email): Future[UserProject] = {
    val selector = Json.obj("_id" -> user, "email.email" -> email)
    val update = Json.obj("$set" -> Json.obj("email.$.verified" -> true))
    collection.findAndUpdate(selector, update, fetchNewObject = true).map(_.result[UserProject].get)
  }

  override def validateDibsProject(user: UserID, url: Resource): Future[UserProject] = {
    val selector = Json.obj("_id" -> user, "dibs.url" -> url)
    val update = Json.obj("$set" -> Json.obj("dibs.$.verified" -> true))
    collection.findAndUpdate(selector, update, fetchNewObject = true).map(_.result[UserProject].get)
  }

  override def deleteDibsProject(user: UserID, url: String): Future[UserProject] = {
    val selector = Json.obj("_id" -> user)
    val update = Json.obj("$pull" -> Json.obj("dibs" -> Json.obj("url" -> url)))
    collection.findAndUpdate(selector, update, fetchNewObject = true).map(_.result[UserProject].get)
  }

  override def deleteEmailProject(user: UserID, email: Email): Future[UserProject] = {
    val selector = Json.obj("_id" -> user)
    val update = Json.obj("$pull" -> Json.obj("email" -> Json.obj("email" -> email)))
    collection.findAndUpdate(selector, update, fetchNewObject = true).map(_.result[UserProject].get)
  }

  override def saveProject(project: Project): Future[Project] = {
    val selector = Json.obj("_id" -> project.user,
      "$or" -> Seq(
        Json.obj("dibs.url" -> project.url),
        Json.obj("google.url" -> project.url),
        Json.obj("tumblr.url" -> project.url),
        Json.obj("email.url" -> project.url)
      ),
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