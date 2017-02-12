package com.clemble.thank.controller

import akka.stream.Materializer
import com.clemble.thank.model.User
import com.clemble.thank.test.util.UserGenerator
import org.specs2.concurrent.ExecutionEnv
import play.api.libs.json.Json
import play.api.test.FakeRequest

class UserControllerSpec(implicit ee: ExecutionEnv) extends ControllerSpec {

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
  }

}
