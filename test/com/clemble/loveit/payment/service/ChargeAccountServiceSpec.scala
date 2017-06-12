package com.clemble.loveit.payment.service

import com.clemble.loveit.common.ServiceSpec
import com.clemble.loveit.payment.service.repository.{ChargeAccountRepository}

class ChargeAccountServiceSpec extends ServiceSpec with TestStripeUtils {

  val service = dependency[ChargeAccountService]
  val repo = dependency[ChargeAccountRepository]

  "STRIPE" should {

    "update bank details" in {
      val user = createUser()
      val chAccBefore = await(repo.getChargeAccount(user))

      val token = someValidStripeToken()
      await(service.updateChargeAccount(user, token))

      repo.getChargeAccount(user) shouldNotEqual chAccBefore
    }

  }

}
