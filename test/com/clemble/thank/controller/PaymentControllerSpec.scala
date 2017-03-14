package com.clemble.thank.controller

import akka.stream.scaladsl.Sink
import com.clemble.thank.model.{Payment, ResourceOwnership}
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner
import play.api.libs.json.Json
import play.api.test.FakeRequest

@RunWith(classOf[JUnitRunner])
class PaymentControllerSpec extends ControllerSpec {

  "GET" should {

    "List on new user" in {
      val userAuth = createUser()
      val req = FakeRequest(GET, s"/api/v1/transaction/user/me").withHeaders(userAuth:_*)
      val fRes = route(application, req).get

      val res = await(fRes)
      val respSource = res.body.dataStream.map(byteStream => Json.parse(byteStream.utf8String).as[Payment])
      val payments = await(respSource.runWith(Sink.seq[Payment]))
      payments shouldEqual Nil
    }

  }

}
