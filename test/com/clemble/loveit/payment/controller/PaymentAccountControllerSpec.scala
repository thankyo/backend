package com.clemble.loveit.payment.controller

class PaymentAccountControllerSpec extends PaymentControllerTestExecutor {

  "Update ChargeAccount" in {
    val user = createUser()

    val chAccBefore = getChargeAccount(user)
    val updatedChAcc = addChargeAccount(user)

    chAccBefore shouldNotEqual Some(updatedChAcc)

    eventually(getChargeAccount(user) shouldEqual Some(updatedChAcc))
  }

}
