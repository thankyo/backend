package com.clemble.thank.controller

import akka.stream.Materializer
import com.clemble.thank.model.{User, UserId}
import com.clemble.thank.service.repository.UserRepository
import com.clemble.thank.test.util.{ThankSpecification, UserGenerator}
import play.api.libs.json.Json
import play.api.test.FakeRequest

import scala.concurrent.ExecutionContext

trait ControllerSpec extends ThankSpecification {

  implicit val materializer = application.injector.instanceOf[Materializer]
  implicit val ec = application.injector.instanceOf[ExecutionContext]

  lazy val userRep = application.injector.instanceOf[UserRepository]

  def createUser(user: User = UserGenerator.generate()): User = {
    await(userRep.save(user))
    user
  }

  def getUser(id: UserId): Option[User] = {
    val readReq = FakeRequest(GET, s"/api/v1/user/${id}")
    val resp = await(route(application, readReq).get)
    resp.header.status match {
      case NOT_FOUND => None
      case OK => Json.parse(await(resp.body.consumeData).utf8String).asOpt[User]
    }
  }

}
