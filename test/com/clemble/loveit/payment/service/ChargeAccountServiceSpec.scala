package com.clemble.loveit.payment.service

import com.clemble.loveit.payment.service.repository.PaymentAccountRepository

class ChargeAccountServiceSpec extends PaymentServiceTestExecutor {

  val service = dependency[PaymentAccountService]
  val repo = dependency[PaymentAccountRepository]

  "STRIPE" should {

    "update bank details" in {
      val user = createUser()
      val chAccBefore = getChargeAccount(user)

      addChargeAccount(user)
      getChargeAccount(user) shouldNotEqual chAccBefore
    }

  }

}
