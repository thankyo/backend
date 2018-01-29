package com.clemble.loveit.common

import java.util.concurrent.ConcurrentHashMap

import akka.stream.Materializer
import akka.stream.scaladsl.{Sink, Source}
import akka.util.ByteString
import com.clemble.loveit.auth.model.AuthResponse
import com.clemble.loveit.auth.model.requests.RegisterRequest
import com.clemble.loveit.common.model.{Resource, UserID}
import com.clemble.loveit.payment.model.PendingTransaction
import com.clemble.loveit.thank.model.OpenGraphObject
import com.clemble.loveit.thank.service.ROService
import com.clemble.loveit.user.model.User.socialProfileJsonFormat
import com.clemble.loveit.user.model._
import com.nimbusds.jose.JWSObject
import play.api.http.Writeable
import play.api.libs.json.{Json, Reads}
import play.api.mvc.Result
import play.api.test.FakeRequest

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext}

trait ControllerSpec extends FunctionalThankSpecification {

  implicit val ec = dependency[ExecutionContext]

  val ownershipService = dependency[ROService]

  implicit class ByteSourceReader(source: Source[ByteString, _]) {
    def read(): String = await(source.runWith(Sink.fold("")((agg, s) => agg.concat(s.utf8String))))
    def readJson[T]()(implicit reader: Reads[T]): Option[T] = Json.parse(read()).asOpt[T]
  }

  override def createUser(profile: RegisterRequest = someRandom[RegisterRequest]): UserID = {
    val req = FakeRequest(POST, "/api/v1/auth/register").withJsonBody(Json.toJson(profile))
    val fRes = route(application, req).get

    val res = await(fRes)
    if (res.header.status != 200)
      throw new Exception()

    val user = ControllerSpec.setUser(res)
    user
  }

  override def getUser(user: UserID): Option[User] = {
    val req = FakeRequest(GET, s"/api/v1/user/${user}/profile")
    val res = await(route(application, req).get)

    res.header.status match {
      case NOT_FOUND => None
      case OK => res.body.dataStream.readJson[User]
    }
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
    val resOpt = Some(await(ownershipService.assignOwnership(user, own)))

    val ogObj = someRandom[OpenGraphObject].copy(url = own.uri)
    val createOgObjReq = FakeRequest(POST, "/api/v1/thank/graph").withJsonBody(Json.toJson(ogObj))
    val resp = await(route(application, createOgObjReq).get)
    if (resp.header.status != OK) {
      throw new IllegalArgumentException("Could not create OG obj for resource")
    }

    resOpt
  }

  def getMyUser(user: UserID): User = {
    val readReq = sign(user, FakeRequest(GET, s"/api/v1/user/my/profile"))
    val resp = await(route(application, readReq).get)
    val userOpt = resp.header.status match {
      case NOT_FOUND => None
      case OK => Json.parse(await(resp.body.consumeData).utf8String).asOpt[User]
    }
    userOpt.get
  }

  def getMyPending(user: String): Seq[PendingTransaction] = {
    val req = sign(user, FakeRequest(GET, s"/api/v1/payment/my/pending"))
    val fRes = route(application, req).get

    val res = await(fRes)
    val payments = res.body.consumeData(materializer).
      map(byteStream => byteStream.utf8String).
      map(str => {
        Json.parse(str).as[List[PendingTransaction]]
      })

    await(payments)
  }

}

object ControllerSpec {

  val userToAuth: ConcurrentHashMap[UserID, Seq[(String, String)]] = new ConcurrentHashMap[String, Seq[(String, String)]]

  def setUser(res: Result)(implicit m: Materializer): String = {
    val authRes = Json.parse(Await.result(res.body.consumeData, 30 second).utf8String).as[AuthResponse]
    val jsonStr = JWSObject.parse(authRes.token).getPayload.toString
    val user = (Json.parse(jsonStr) \ "id").as[String]

    userToAuth.put(user, Seq("X-Auth-Token" -> authRes.token))
    user
  }

  def getUser(user: UserID) = {
    require(userToAuth.get(user) != null)
    userToAuth.get(user)
  }

}
