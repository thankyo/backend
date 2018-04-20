package com.clemble.loveit.thank

import java.time.LocalDateTime

import akka.stream.Materializer
import com.clemble.loveit.common.model.{Project, User}
import com.clemble.loveit.common.mongo.{JSONCollectionFactory, MongoSafeUtils}
import com.clemble.loveit.thank.model.UserProjects
import javax.inject.{Inject, Singleton}
import play.api.libs.json.{JsObject, JsString, Json}
import reactivemongo.play.json._

import scala.concurrent.{Await, ExecutionContext, Future}
import scala.concurrent.duration._

trait DatabaseUpdater {

  def updateCollections(): Future[Boolean]

}

@Singleton
class SimpleDatabaseUpdater @Inject() (factory: JSONCollectionFactory, implicit val ec: ExecutionContext, implicit val m: Materializer) extends DatabaseUpdater {

  val versionCollection = factory.create("appVersion")

  val UPDATES: List[() => Future[Boolean]] = List(
    createUserProjectForExistingUsers,
    moveFromProjectsToUserProject,
    updateUserProject
  )

  Await.result(updateCollections(), 5 minutes)

  def getVersion(): Future[Int] = {
    versionCollection.find(Json.obj(), Json.obj("version" -> 1))
      .sort(Json.obj("version" -> -1))
      .one[JsObject]
      .map(_.flatMap(json => (json \ "version").asOpt[Int]).getOrElse(0))
  }

  def updateVersion(version: Int): Future[Boolean] = {
    versionCollection.insert(Json.obj("version" -> version, "created" -> LocalDateTime.now())).map(_.ok)
  }

  def sequentiallyProcess(updates: List[() => Future[Boolean]], agg: Future[Int] = Future.successful(0)): Future[Int] = {
    updates match {
      case Nil => agg
      case x :: xs =>
        x().flatMap({
          case true => sequentiallyProcess(xs, agg.map(_ + 1))
          case false => agg
        })
    }
  }

  def updateCollections(): Future[Boolean] = {
    for {
      version <- getVersion()
      relevantUpdates = UPDATES.drop(version)
      updated <- sequentiallyProcess(relevantUpdates)
      nextVersion = version + updated
      updatedVersion <- updateVersion(nextVersion)
    } yield {
      nextVersion == relevantUpdates.length && updatedVersion
    }
  }

  def createUserProjectForExistingUsers(): Future[Boolean] = {
    val userCollection = factory.create("user")
    val userProjectsCollection = factory.create("userProject")
    MongoSafeUtils.findAll[User](userCollection, Json.obj()).runFoldAsync(true)((agg, user) => {
      agg match {
        case false => Future.successful(false)
        case true => userProjectsCollection.find(Json.obj("_id" -> user.id)).one[UserProjects].flatMap({
          case Some(_) => Future.successful(true)
          case None => userProjectsCollection.insert[UserProjects](UserProjects from user).map(_.ok)
        })
      }
    }).recover({ case _ => false })
  }

  def moveFromProjectsToUserProject(): Future[Boolean] = {
    val projectsCollection = factory.create("projects")
    val userProjectsCollection = factory.create("userProject")
    MongoSafeUtils.findAll[Project](projectsCollection, Json.obj()).runFoldAsync(true)((agg, project) => {
      agg match {
        case false => Future.successful(false)
        case true =>
          val selector = Json.obj("_id" -> project.user)
          val update = Json.obj("$addToSet" -> Json.obj("installed" -> project))
          userProjectsCollection.update(selector, update).map(_.ok)
      }
    }).recover({ case _ => false })
  }

  def updateUserProject(): Future[Boolean] = {
    val userProjectsCollection = factory.create("userProject")
    MongoSafeUtils.findAll[JsObject](userProjectsCollection, Json.obj()).runFoldAsync(true)((agg, json) => {
      agg match {
        case false => Future.successful(false)
        case true =>
          val id = (json \ "_id").as[String]
          val user = (json \ "user").as[String]
          if (id == user) {
            Future.successful(true)
          } else {
            userProjectsCollection.remove(Json.obj("_id" -> id)).flatMap(_.ok match {
              case true => userProjectsCollection.insert(json + ("_id" -> JsString("user"))).map(_.ok)
              case false => Future.successful(false)
            })
          }
      }
    })
  }

}