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
import com.nimbusds.jose.JWSObject
import play.api.http.Writeable
import play.api.libs.json.{Json, Reads}
import play.api.mvc.Result
import play.api.test.FakeRequest

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext}

trait ControllerSpec extends FunctionalThankSpecification {

  implicit val ec = dependency[ExecutionContext]

  val prjRepo = dependency[ProjectRepository]

  implicit class ByteSourceReader(source: Source[ByteString, _]) {
    def read(): String = await(source.runWith(Sink.fold("")((agg, s) => agg.concat(s.utf8String))))
    def readJson[T]()(implicit reader: Reads[T]): Option[T] = Json.parse(read()).asOpt[T]
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
      case OK => res.body.dataStream.readJson[User]
    }
  }

  def sign[A](user: UserID, req: FakeRequest[A]): FakeRequest[A] = {
    val userAuth = ControllerSpec.getUser(user)
    req.withHeaders(userAuth:_*)
  }

  def perform[A](user: UserID, req: FakeRequest[A])(implicit writeable: Writeable[A]) = {
    val singed = sign(user, req)
    val fRes = route(application, singed).get
    await(fRes)
  }

  override def createProject(user: UserID = createUser(), url: Resource = randomResource): Project = {
    val project = await(prjRepo.save(Project(url, user, someRandom[String], someRandom[String], someRandom[Verification])))

    val ogObj = someRandom[OpenGraphObject].copy(url = url)
    val createOgObjReq = FakeRequest(POST, "/api/v1/thank/graph").withJsonBody(Json.toJson(ogObj))
    val resp = await(route(application, createOgObjReq).get)
    if (resp.header.status != OK) {
      throw new IllegalArgumentException("Could not create OG obj for resource")
    }

    project
  }

  def getMyUser(user: UserID): User = {
    val resp = perform(user, FakeRequest(GET, s"/api/v1/user/my/profile"))
    val userOpt = resp.header.status match {
      case NOT_FOUND => None
      case OK => Json.parse(await(resp.body.consumeData).utf8String).asOpt[User]
    }
    userOpt.get
  }

  def pendingCharges(user: String): Seq[PendingTransaction] = {
    val req = sign(user, FakeRequest(GET, s"/api/v1/payment/my/charge/pending"))
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
