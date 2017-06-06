package com.clemble.loveit.common

import akka.stream.scaladsl.Sink
import com.clemble.loveit.common.model.{Resource, UserID}
import com.clemble.loveit.payment.model.ThankTransaction
import com.clemble.loveit.thank.service.ResourceOwnershipService
import com.clemble.loveit.user.model.User.socialProfileJsonFormat
import com.clemble.loveit.user.model._
import com.clemble.loveit.user.service.repository.UserRepository
import com.mohiva.play.silhouette.impl.providers.CommonSocialProfile
import com.nimbusds.jose.JWSObject
import play.api.libs.json.Json
import play.api.test.FakeRequest

import scala.concurrent.ExecutionContext

trait ControllerSpec extends ThankSpecification {

  implicit val ec = dependency[ExecutionContext]

  val userRep = dependency[UserRepository]
  val ownershipService = dependency[ResourceOwnershipService]

  def createUser(socialProfile: CommonSocialProfile = someRandom[CommonSocialProfile]): String = {
    val req = FakeRequest(POST, "/api/v1/auth/authenticate/test").withJsonBody(Json.toJson(socialProfile))
    val fRes = route(application, req).get

    val res = await(fRes)
    res.header.status must beEqualTo(200)

    val bodyStr = await(res.body.consumeData).utf8String
    val jsonStr = JWSObject.parse(bodyStr).getPayload.toString
    val user = (Json.parse(jsonStr) \ "id").as[String]

    ControllerSpec.setUser(user, Seq("X-Auth-Token" -> bodyStr))

    user
  }

  def sign[A](user: UserID, req: FakeRequest[A]) = {
    val userAuth = ControllerSpec.getUser(user)
    req.withHeaders(userAuth:_*)
  }

  def addOwnership(user: UserID, own: Resource): Option[Resource] = {
    Some(await(ownershipService.assignOwnership(user, own)))
  }

  def getMyUser(user: UserID): User = {
    getUser(user).get
  }

  def getUser(id: UserID): Option[User] = {
    val readReq = sign(id, FakeRequest(GET, s"/api/v1/user/my"))
    val resp = await(route(application, readReq).get)
    resp.header.status match {
      case NOT_FOUND => None
      case OK => Json.parse(await(resp.body.consumeData).utf8String).asOpt[User]
    }
  }

  def getMyPayments(user: String): Seq[ThankTransaction] = {
    val req = sign(user, FakeRequest(GET, s"/api/v1/transaction/user/my"))
    val fRes = route(application, req).get

    val res = await(fRes)
    val respSource = res.body.dataStream.map(byteStream => Json.parse(byteStream.utf8String).as[ThankTransaction])
    val payments = await(respSource.runWith(Sink.seq[ThankTransaction]))
    payments
  }

}

object ControllerSpec {

  var userToAuth: Map[UserID, Seq[(String, String)]] = Map.empty[String, Seq[(String, String)]]

  def setUser(user: UserID, headers: Seq[(String, String)]) = {
    userToAuth = userToAuth + (user -> headers)
    userToAuth
  }

  def getUser(user: UserID) = userToAuth(user)

}
