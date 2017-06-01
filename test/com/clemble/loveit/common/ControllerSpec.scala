package com.clemble.loveit.common

import akka.stream.scaladsl.Sink
import com.clemble.loveit.common.model.{Amount, Resource, UserID}
import com.clemble.loveit.payment.model.ThankTransaction
import com.clemble.loveit.test.util.CommonSocialProfileGenerator
import com.clemble.loveit.thank.service.ResourceOwnershipService
import com.clemble.loveit.user.model.User.socialProfileJsonFormat
import com.clemble.loveit.user.model._
import com.clemble.loveit.user.service.repository.UserRepository
import com.mohiva.play.silhouette.impl.providers.CommonSocialProfile
import play.api.libs.json.Json
import play.api.test.FakeRequest

import scala.concurrent.ExecutionContext

trait ControllerSpec extends ThankSpecification {

  implicit val ec = dependency[ExecutionContext]

  val userRep = dependency[UserRepository]
  val ownershipService = dependency[ResourceOwnershipService]

  def createUser(socialProfile: CommonSocialProfile = CommonSocialProfileGenerator.generate(), balance: Amount = 200): Seq[(String, String)] = {
    val req = FakeRequest(POST, "/api/v1/auth/authenticate/test").withJsonBody(Json.toJson(socialProfile))
    val fRes = route(application, req).get

    val res = await(fRes)
    res.header.status must beEqualTo(200)

    val bodyStr = await(res.body.consumeData).utf8String
    val resp = Seq("X-Auth-Token" -> bodyStr)

    // TODO REMOVE this is a hack not to implement fake payments
    userRep.changeBalance(getMyUser()(resp).id, balance)

    resp
  }

  def addOwnership(user: UserID, own: Resource)(implicit authHeader: Seq[(String, String)]): Option[Resource] = {
    Some(await(ownershipService.assignOwnership(user, own)))
  }

  def getMyUser()(implicit authHeader: Seq[(String, String)]): User = {
    getUser("my").get
  }

  def getUser(id: UserID)(implicit authHeader: Seq[(String, String)]): Option[User] = {
    val readReq = FakeRequest(GET, s"/api/v1/user/${id}").withHeaders(authHeader:_*)
    val resp = await(route(application, readReq).get)
    resp.header.status match {
      case NOT_FOUND => None
      case OK => Json.parse(await(resp.body.consumeData).utf8String).asOpt[User]
    }
  }

  def getMyPayments()(implicit authHeaders: Seq[(String, String)]): Seq[ThankTransaction] = {
    val req = FakeRequest(GET, s"/api/v1/transaction/user/my").withHeaders(authHeaders:_*)
    val fRes = route(application, req).get

    val res = await(fRes)
    val respSource = res.body.dataStream.map(byteStream => Json.parse(byteStream.utf8String).as[ThankTransaction])
    val payments = await(respSource.runWith(Sink.seq[ThankTransaction]))
    payments
  }

}
