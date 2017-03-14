package com.clemble.thank.controller

import akka.stream.Materializer
import akka.stream.scaladsl.Sink
import com.clemble.thank.model.{Payment, ResourceOwnership, User, UserId}
import com.clemble.thank.test.util.{CommonSocialProfileGenerator, ThankSpecification, UserGenerator}
import com.mohiva.play.silhouette.impl.providers.CommonSocialProfile
import play.api.libs.json.Json
import play.api.test.FakeRequest
import com.clemble.thank.model.User.socialProfileJsonFormat

import scala.concurrent.ExecutionContext

trait ControllerSpec extends ThankSpecification {

  implicit val ec = application.injector.instanceOf[ExecutionContext]

  def createUser(socialProfile: CommonSocialProfile = CommonSocialProfileGenerator.generate()): Seq[(String, String)] = {
    val req = FakeRequest(POST, "/api/v1/auth/authenticate/test").withJsonBody(Json.toJson(socialProfile))
    val fRes = route(application, req).get

    val res = await(fRes)
    res.header.status must beEqualTo(200)

    val bodyStr = await(res.body.consumeData).utf8String
    Seq("X-Auth-Token" -> bodyStr)
  }

  def addOwnership(own: ResourceOwnership)(implicit authHeader: Seq[(String, String)]): Option[ResourceOwnership] = {
    val req = FakeRequest(POST, "/api/v1/ownership/me").withJsonBody(Json.toJson(own)).withHeaders(authHeader:_*)
    val fRes = route(application, req).get

    val res = await(fRes)
    res.header.status match {
      case NOT_FOUND => None
      case CREATED => Json.parse(await(res.body.consumeData).utf8String).asOpt[ResourceOwnership]
    }
  }

  def getMyUser()(implicit authHeader: Seq[(String, String)]): User = {
    getUser("me").get
  }

  def getUser(id: UserId)(implicit authHeader: Seq[(String, String)]): Option[User] = {
    val readReq = FakeRequest(GET, s"/api/v1/user/${id}").withHeaders(authHeader:_*)
    val resp = await(route(application, readReq).get)
    resp.header.status match {
      case NOT_FOUND => None
      case OK => Json.parse(await(resp.body.consumeData).utf8String).asOpt[User]
    }
  }

  def getMyPayments()(implicit authHeaders: Seq[(String, String)]): Seq[Payment] = {
    val req = FakeRequest(GET, s"/api/v1/transaction/user/me").withHeaders(authHeaders:_*)
    val fRes = route(application, req).get

    val res = await(fRes)
    val respSource = res.body.dataStream.map(byteStream => Json.parse(byteStream.utf8String).as[Payment])
    val payments = await(respSource.runWith(Sink.seq[Payment]))
    payments
  }

}
