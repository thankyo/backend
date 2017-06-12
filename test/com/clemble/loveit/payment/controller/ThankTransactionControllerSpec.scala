package com.clemble.loveit.payment.controller

import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner

@RunWith(classOf[JUnitRunner])
class ThankTransactionControllerSpec extends PaymentControllerSpec {

  "GET" should {

    "List on new user" in {
      val user = createUser()
      val pending = pendingThanks(user)
      pending shouldEqual Nil
    }

  }

}
