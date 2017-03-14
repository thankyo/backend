package com.clemble.thank.controller

import com.clemble.thank.model.{Payment, ResourceOwnership}
import play.api.libs.json.Json
import play.api.test.FakeRequest

class PaymentControllerSpec extends ControllerSpec {

  "GET" should {

    "List on new user" in {
      val userAuth = createUser()
      val req = FakeRequest(GET, s"/api/v1/transaction/user/me").withHeaders(userAuth:_*)
      val fRes = route(application, req).get

      val res = await(fRes)
      val payments = Json.parse(await(res.body.consumeData).utf8String).as[List[Payment]]
      payments shouldEqual Nil
    }

  }

}
