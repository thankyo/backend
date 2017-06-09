package com.clemble.loveit.payment.service

import com.clemble.loveit.common.ServiceSpec
import com.clemble.loveit.payment.service.repository.{BankDetailsRepository}

class BankDetailsServiceSpec extends ServiceSpec with TestStripeUtils {

  val service = dependency[BankDetailsService]
  val repo = dependency[BankDetailsRepository]

  "STRIPE" should {

    "update bank details" in {
      val user = createUser()
      val bankDetailsBefore = await(repo.getBankDetails(user))

      val token = someValidStripeToken()
      await(service.updateBankDetails(user, token))

      repo.getBankDetails(user) shouldNotEqual bankDetailsBefore
    }

  }

}
