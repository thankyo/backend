package com.clemble.loveit.payment.controller

class ChargeAccountControllerSpec extends PaymentControllerSpec {

  "Update ChargeAccount" in {
    val user = createUser()

    val chAccBefore = getChargeAccount(user)
    val updatedChAcc = Some(addChargeAccount(user, someValidStripeToken()))

    val chAccAfter = getChargeAccount(user)

    chAccAfter shouldEqual updatedChAcc
    chAccBefore shouldNotEqual chAccAfter
  }

}
