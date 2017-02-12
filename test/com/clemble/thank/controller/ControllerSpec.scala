package com.clemble.thank.controller

import akka.stream.Materializer
import com.clemble.thank.model.{User, UserId}
import com.clemble.thank.test.util.UserGenerator
import play.api.Mode
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import play.api.test.{FakeRequest, PlaySpecification}

import scala.concurrent.ExecutionContext

trait ControllerSpec extends PlaySpecification {

  implicit val application = new GuiceApplicationBuilder().
    in(Mode.Test).
    build

  implicit val materializer = application.injector.instanceOf[Materializer]
  implicit val ec = application.injector.instanceOf[ExecutionContext]

  def createUser(user: User = UserGenerator.generate()): User = {
    val req = FakeRequest("POST", "/user").withJsonBody(Json.toJson(user))
    val fRes = route(application, req).get

    val res = await(fRes)
    res.header.status must beEqualTo(201)

    val bodyStr = await(res.body.consumeData).utf8String
    val readUser = Json.parse(bodyStr).as[User]
    user shouldEqual readUser

    user
  }

  def getUser(id: UserId): Option[User] = {
    val readReq = FakeRequest(GET, s"/user/${id}")
    val resp = await(route(application, readReq).get)
    resp.header.status match {
      case NOT_FOUND => None
      case OK => Json.parse(await(resp.body.consumeData).utf8String).asOpt[User]
    }
  }

}
