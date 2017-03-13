package com.clemble.thank.controller

import akka.stream.Materializer
import com.clemble.thank.model.{User, UserId}
import com.clemble.thank.service.repository.UserRepository
import com.clemble.thank.test.util.{ThankSpecification, UserGenerator}
import com.clemble.thank.util.AuthEnv
import com.mohiva.play.silhouette.api.LoginInfo
import play.api.libs.json.Json
import play.api.test.FakeRequest
import com.mohiva.play.silhouette.test._

import scala.concurrent.ExecutionContext

trait ControllerSpec extends ThankSpecification {

  implicit val materializer = application.injector.instanceOf[Materializer]
  implicit val ec = application.injector.instanceOf[ExecutionContext]

  lazy val userRep = application.injector.instanceOf[UserRepository]

  def createUser(user: User = UserGenerator.generate()): User = {
    await(userRep.save(user))
  }

  def getUser(id: UserId): Option[User] = {
    val loginInfo = LoginInfo("fake", id)
    val identity = User(id).copy(profiles = Set(loginInfo))
    implicit val env = FakeEnvironment[AuthEnv](Seq(loginInfo -> identity.toIdentity()))

    val readReq = FakeRequest(GET, s"/api/v1/user/${id}").withAuthenticator(loginInfo)
    val resp = await(route(application, readReq).get)
    resp.header.status match {
      case NOT_FOUND => None
      case OK => Json.parse(await(resp.body.consumeData).utf8String).asOpt[User]
    }
  }

}
