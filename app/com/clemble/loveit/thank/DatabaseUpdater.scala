package com.clemble.loveit.thank

import java.time.LocalDateTime

import akka.stream.Materializer
import com.clemble.loveit.common.model.{Project, User}
import com.clemble.loveit.common.mongo.{JSONCollectionFactory, MongoSafeUtils}
import com.clemble.loveit.thank.model.UserProject
import javax.inject.{Inject, Singleton}
import play.api.libs.json._
import reactivemongo.play.json._
import reactivemongo.play.json.collection.JSONCollection

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future}

trait DatabaseUpdater {

  def updateCollections(): Future[Boolean]

}

@Singleton
class SimpleDatabaseUpdater @Inject()(factory: JSONCollectionFactory, implicit val ec: ExecutionContext, implicit val m: Materializer) extends DatabaseUpdater {

  val versionCollection = factory.create("appVersion")

  val UPDATES: List[() => Future[Boolean]] = List(
    createUserProjectForExistingUsers,
    moveFromProjectsToUserProject,
    updateUserProjectsStructure,
    updateDibsFlags
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
      updatedVersion <- if (nextVersion == version) Future.successful(true) else updateVersion(nextVersion)
    } yield {
      nextVersion == relevantUpdates.length && updatedVersion
    }
  }

  def findAndProcessAll[T](collection: JSONCollection, update: (T) => Future[Boolean])(implicit format: Reads[T]): Future[Boolean] = {
    MongoSafeUtils.findAll[T](collection, Json.obj()).runFoldAsync(true)({
      case (false, _) => Future.successful(false)
      case (_, el) => update(el)
    }).recover({ case _ => false })
  }

  def createUserProjectForExistingUsers(): Future[Boolean] = {
    val userCollection = factory.create("user")
    val userProjectsCollection = factory.create("userProject")
    MongoSafeUtils.findAll[User](userCollection, Json.obj()).runFoldAsync(true)((agg, user) => {
      agg match {
        case false => Future.successful(false)
        case true => userProjectsCollection.find(Json.obj("_id" -> user.id)).one[UserProject].flatMap({
          case Some(_) => Future.successful(true)
          case None =>
            val userProjectJson = Json.toJsObject(UserProject from user) + ("_id" -> JsString(user.id))
            userProjectsCollection.insert(userProjectJson).map(_.ok)
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

  def updateUserProjectsStructure(): Future[Boolean] = {
    val userProjectsCollection = factory.create("userProject")
    MongoSafeUtils.findAll[JsObject](userProjectsCollection, Json.obj()).runFoldAsync(true)({
      case (false, _) => Future.successful(false)
      case (true, usrPrj) if (usrPrj \ "owned").asOpt[List[JsObject]].isEmpty => Future.successful(true)
      case (true, usrPrj) =>
        val selector = Json.obj("_id" -> (usrPrj \ "_id").as[String])
        val owned = (usrPrj \ "owned").as[List[JsObject]]

        val dibs = owned.filter(prj => (prj \ "verification").asOpt[String].contains("dibs"))
        val tumblr = owned.filter(prj => (prj \ "verification").asOpt[String].contains("tumblr"))
        val google = owned.filter(prj => (prj \ "verification").asOpt[String].contains("google"))
        val email = Json.arr()

        val update = userProjectsCollection.update(selector, Json.obj("$set" -> Json.obj(
          "dibs" -> dibs,
          "tumblr" -> tumblr,
          "google" -> google,
          "email" -> email
        )))
        MongoSafeUtils.safeSingleUpdate(update)
    })
  }

  def updateDibsFlags(): Future[Boolean] = {
    val collection = factory.create("userProject")
    val update = (usrPrj: JsObject) => {
      val dibs = (usrPrj \ "dibs").as[List[JsObject]]
      val needsUpdate = dibs.exists(prj => (prj \ "verified").isEmpty)
      if (needsUpdate) {
        val selector = Json.obj("_id" -> (usrPrj \ "_id").as[String])
        val modification = Json.obj("$set" -> Json.obj("dibs" -> dibs.map(prj => prj + ("verified" -> JsBoolean(false)))))
        collection.update(selector, modification).map(_.ok)
      } else {
        Future.successful(true)
      }
    }
    findAndProcessAll[JsObject](collection, update)
  }

}