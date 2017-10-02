package com.clemble.loveit.payment.service

import com.clemble.loveit.payment.service.repository.{ChargeAccountRepository}

class ChargeAccountServiceSpec extends PaymentServiceTestExecutor {

  val service = dependency[ChargeAccountService]
  val repo = dependency[ChargeAccountRepository]

  "STRIPE" should {

    "update bank details" in {
      val user = createUser()
      val chAccBefore = getChargeAccount(user)

      addChargeAccount(user)
      getChargeAccount(user) shouldNotEqual chAccBefore
    }

  }

}
