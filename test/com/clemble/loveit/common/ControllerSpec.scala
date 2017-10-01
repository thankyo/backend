package com.clemble.loveit.common

import java.util.concurrent.ConcurrentHashMap

import akka.stream.Materializer
import akka.stream.scaladsl.{Sink, Source}
import akka.util.ByteString
import com.clemble.loveit.auth.models.requests.SignUpRequest
import com.clemble.loveit.common.model.{Resource, UserID}
import com.clemble.loveit.payment.model.ThankTransaction
import com.clemble.loveit.thank.service.ROService
import com.clemble.loveit.user.model.User.socialProfileJsonFormat
import com.clemble.loveit.user.model._
import com.clemble.loveit.user.service.repository.UserRepository
import com.mohiva.play.silhouette.impl.providers.CommonSocialProfile
import com.nimbusds.jose.JWSObject
import play.api.http.Writeable
import play.api.libs.json.{Json, Reads}
import play.api.mvc.Result
import play.api.test.FakeRequest

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext}

trait ControllerSpec extends FunctionalThankSpecification {

  implicit val ec = dependency[ExecutionContext]

  val userRep = dependency[UserRepository]
  val ownershipService = dependency[ROService]

  implicit class ByteSourceReader(source: Source[ByteString, _]) {
    def read(): String = await(source.runWith(Sink.fold("")((agg, s) => agg.concat(s.utf8String))))
    def readJson[T]()(implicit reader: Reads[T]): Option[T] = Json.parse(read()).asOpt[T]
  }

  override def createUser(profile: SignUpRequest = someRandom[SignUpRequest]): UserID = {
    val req = FakeRequest(POST, "/api/v1/auth/signUp").withJsonBody(Json.toJson(profile))
    val fRes = route(application, req).get

    val res = await(fRes)
    val user = ControllerSpec.setUser(res)
    user
  }

  def sign[A](user: UserID, req: FakeRequest[A]) = {
    val userAuth = ControllerSpec.getUser(user)
    req.withHeaders(userAuth:_*)
  }

  def perform[A](user: UserID, req: FakeRequest[A])(implicit writeable: Writeable[A]) = {
    val singed = sign(user, req)
    val fRes = route(application, singed).get
    await(fRes)
  }

  def addOwnership(user: UserID, own: Resource): Option[Resource] = {
    Some(await(ownershipService.assignOwnership(user, own)))
  }

  def getMyUser(user: UserID): User = {
    getUser(user).get
  }

  def getUser(id: UserID): Option[User] = {
    val readReq = sign(id, FakeRequest(GET, s"/api/v1/user/my/profile"))
    val resp = await(route(application, readReq).get)
    resp.header.status match {
      case NOT_FOUND => None
      case OK => Json.parse(await(resp.body.consumeData).utf8String).asOpt[User]
    }
  }

  def getMyPayments(user: String): Seq[ThankTransaction] = {
    val req = sign(user, FakeRequest(GET, s"/api/v1/payment/my/pending"))
    val fRes = route(application, req).get

    val res = await(fRes)
    val payments = res.body.consumeData(materializer).
      map(byteStream => byteStream.utf8String).
      map(str => {
        Json.parse(str).as[List[ThankTransaction]]
      })

    await(payments)
  }

}

object ControllerSpec {

  var userToAuth: ConcurrentHashMap[UserID, Seq[(String, String)]] = new ConcurrentHashMap[String, Seq[(String, String)]]

  def setUser(res: Result)(implicit m: Materializer): String = {
    val bodyStr = Json.parse(Await.result(res.body.consumeData, 30 second).utf8String).as[String]
    val jsonStr = JWSObject.parse(bodyStr).getPayload.toString
    val user = (Json.parse(jsonStr) \ "id").as[String]

    userToAuth.put(user, Seq("X-Auth-Token" -> bodyStr))
    user
  }

  def getUser(user: UserID) = {
    require(userToAuth.get(user) != null)
    userToAuth.get(user)
  }

}
