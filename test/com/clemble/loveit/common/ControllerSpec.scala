package com.clemble.loveit.common

import java.util.concurrent.ConcurrentHashMap

import akka.stream.Materializer
import akka.stream.scaladsl.{Sink, Source}
import akka.util.ByteString
import com.clemble.loveit.auth.model.AuthResponse
import com.clemble.loveit.auth.model.requests.RegistrationRequest
import com.clemble.loveit.common.model.{OpenGraphObject, Project, Resource, UserID}
import com.clemble.loveit.payment.model.PendingTransaction
import com.clemble.loveit.thank.service.repository.ProjectRepository
import com.clemble.loveit.common.model.User.socialProfileJsonFormat
import com.clemble.loveit.common.model._
import com.clemble.loveit.thank.model.UserProjects
import com.nimbusds.jose.JWSObject
import play.api.http.Writeable
import play.api.libs.json.{JsObject, JsString, Json, Reads}
import play.api.mvc.Result
import play.api.test.FakeRequest

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext}

trait ControllerSpec extends FunctionalThankSpecification {

  implicit val ec = dependency[ExecutionContext]

  val prjRepo = dependency[ProjectRepository]

  implicit class ByteSourceReader(source: Source[ByteString, _]) {
    def read(): String = await(source.runWith(Sink.fold("")((agg, s) => agg.concat(s.utf8String))))
    def readJsonOpt[T]()(implicit reader: Reads[T]): Option[T] = Json.parse(read()).asOpt[T]
    def readJson[T]()(implicit reader: Reads[T]): T = Json.parse(read()).as[T]
  }

  override def createUser(profile: RegistrationRequest = someRandom[RegistrationRequest]): UserID = {
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
      case OK => res.body.dataStream.readJsonOpt[User]
    }
  }

  def perform[A](user: UserID, req: FakeRequest[A])(implicit writeable: Writeable[A]) = {
    val userAuth = ControllerSpec.getUser(user)
    val signed = req.withHeaders(userAuth:_*)
    val fRes = route(application, signed).get
    await(fRes)
  }

  override def createProject(user: UserID = createUser(), url: Resource = randomResource): Project = {
    val dibsOnPrj = FakeRequest(POST, "/api/v1/thank/user/my/owned/dibs").
      withJsonBody(Json.obj("url" -> url))
    val res = perform(user, dibsOnPrj)

    res.header.status shouldEqual OK
    val ownedPrj = res.body.dataStream.readJson[UserProjects].dibs.find(_.url == url).get

    val createPrjReq = FakeRequest(POST, "/api/v1/thank/project").withJsonBody(Json.toJson(ownedPrj))
    val resp = perform(user, createPrjReq)
    resp.header.status shouldEqual OK

    resp.body.dataStream.readJson[Project]
  }

  def getMyUser(user: UserID): User = {
    val res = perform(user, FakeRequest(GET, s"/api/v1/user/my/profile"))
    res.header.status shouldEqual OK
    res.body.dataStream.readJson[User]
  }

  def pendingCharges(user: String): Seq[PendingTransaction] = {
    val res = perform(user, FakeRequest(GET, s"/api/v1/payment/my/charge/pending"))
    res.body.dataStream.readJson[List[PendingTransaction]]
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
