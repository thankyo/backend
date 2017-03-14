package com.clemble.thank.controller

import akka.stream.Materializer
import com.clemble.thank.model.{ResourceOwnership, User, UserId}
import com.clemble.thank.test.util.{CommonSocialProfileGenerator, ThankSpecification, UserGenerator}
import com.clemble.thank.util.AuthEnv
import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.impl.providers.CommonSocialProfile
import play.api.libs.json.Json
import play.api.test.FakeRequest
import com.mohiva.play.silhouette.test._
import com.clemble.thank.model.User.socialProfileJsonFormat

import scala.concurrent.ExecutionContext

trait ControllerSpec extends ThankSpecification {

  implicit val materializer = application.injector.instanceOf[Materializer]
  implicit val ec = application.injector.instanceOf[ExecutionContext]

  def createUser(socialProfile: CommonSocialProfile = CommonSocialProfileGenerator.generate()): User = {
    val req = FakeRequest(POST, "/api/v1/auth/authenticate/test").withJsonBody(Json.toJson(socialProfile))
    val fRes = route(application, req).get

    val res = await(fRes)
    res.header.status must beEqualTo(200)

    val bodyStr = await(res.body.consumeData).utf8String
    val readUser = Json.parse(bodyStr).as[User]
    // socialProfile shouldEqual readUser

    readUser
  }

  def addOwnership(user: User, own: ResourceOwnership): User = {
    ???
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
