package com.clemble.thank.controller

import akka.stream.Materializer
import com.clemble.thank.model.User
import com.clemble.thank.model.error.{RepositoryError, RepositoryException, ThankException}
import com.clemble.thank.test.util.UserGenerator
import org.specs2.concurrent.ExecutionEnv
import play.api.libs.json.Json
import play.api.test.FakeRequest

class UserControllerSpec(implicit ee: ExecutionEnv) extends ControllerSpec {

  implicit val materializer = application.injector.instanceOf[Materializer]

  "CREATE" should {

    "Support single create" in {
      val user = UserGenerator.generate()
      val req = FakeRequest("POST", "/user").withJsonBody(Json.toJson(user))
      val fRes = route(application, req).get

      val res = await(fRes)
      res.header.status must beEqualTo(201)

      val body = await(res.body.consumeData)
      val savedUser = Json.parse(body.utf8String).as[User]
      savedUser must beEqualTo(user)
    }

    "Return error on same create" in {
      val user = UserGenerator.generate()
      val req = FakeRequest("POST", "/user").withJsonBody(Json.toJson(user))

      await(route(application, req).get)
      val secondRes = await(route(application, req).get)
      secondRes.header.status must beEqualTo(BAD_REQUEST)

      val body = await(secondRes.body.consumeData)
      val exc = Json.parse(body.utf8String).as[RepositoryException]
      exc.errors must containAllOf(Seq(RepositoryError.duplicateKey()))
    }

  }

  "GET" should {

    "return after creation" in {
      val user = UserGenerator.generate()
      val req = FakeRequest("POST", "/user").withJsonBody(Json.toJson(user))
      await(route(application, req).get)

      val readReq = FakeRequest("GET", s"/user/${user.id}")
      val readUserStr = await(route(application, readReq).get.flatMap(_.body.consumeData))

      val readUser = Json.parse(readUserStr.utf8String).as[User]
      readUser must beEqualTo(user)
    }

  }

}
