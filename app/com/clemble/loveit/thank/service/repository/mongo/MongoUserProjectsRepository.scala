package com.clemble.loveit.thank.service.repository.mongo

import com.clemble.loveit.common.model.Project._
import com.clemble.loveit.common.model.{OwnedProject, Project, UserID}
import com.clemble.loveit.common.mongo.MongoSafeUtils
import com.clemble.loveit.thank.model.UserProjects
import com.clemble.loveit.thank.service.repository.UserProjectsRepository
import javax.inject.{Inject, Named}
import play.api.libs.json.{JsString, Json}
import reactivemongo.play.json._
import reactivemongo.play.json.collection.JSONCollection

import scala.concurrent.{ExecutionContext, Future}

class MongoUserProjectsRepository @Inject() (
  @Named("userProject") collection: JSONCollection,
  implicit val ec: ExecutionContext
) extends UserProjectsRepository {

  override def findById(user: UserID): Future[Option[UserProjects]] = {
    val selector = Json.obj("_id" -> user)
    collection.find(selector).one[UserProjects]
  }

  override def save(projects: UserProjects): Future[UserProjects] = {
    val presentation = Json.toJsObject(projects) + ("_id" -> JsString(projects.user))
    MongoSafeUtils.safeSingleUpdate(collection.insert(presentation)).map({
      case true => projects
      case false => throw new IllegalArgumentException(s"Could not update ${projects.user}")
    })
  }

  override def append(user: UserID, owned: OwnedProject): Future[Boolean] = {
    val selector = Json.obj("_id" -> user)
    val update = Json.obj("$addToSet" -> Json.obj("owned" -> owned))
    MongoSafeUtils.safeSingleUpdate(collection.update(selector, update))
  }

  override def saveProject(project: Project): Future[Boolean] = {
    val selector = Json.obj("_id" -> project.user)
    val update = Json.obj("$addToSet" -> Json.obj("installed" -> project))
    MongoSafeUtils.safeSingleUpdate(collection.update(selector, update))
  }

}
