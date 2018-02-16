package com.clemble.loveit.payment.controller

import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner

@RunWith(classOf[JUnitRunner])
class PendingTransactionControllerSpec extends PaymentControllerTestExecutor {

  "GET" should {

    "List on new user" in {
      val user = createUser()
      val pending = outgoingTransactions(user)
      pending shouldEqual Nil
    }

  }

}
