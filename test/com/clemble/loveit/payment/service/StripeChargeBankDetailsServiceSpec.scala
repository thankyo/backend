package com.clemble.loveit.payment.service

import com.clemble.loveit.common.ServiceSpec
import com.clemble.loveit.payment.model.StripeBankDetails

import scala.util.Try

class StripeChargeBankDetailsServiceSpec extends ServiceSpec with TestStripeUtils{

  val service = dependency[ChargeBankDetailsService]

  "STRIPE" should {

    "create bank details" in {
      val token = someValidStripeToken()

      val bankDetails = Try(await(service.process(token)))

      bankDetails.isSuccess shouldEqual true
    }

    "specify card brand  and last4 digits" in {
      val token = someValidStripeToken()

      val bankDetails = await(service.process(token)).asInstanceOf[StripeBankDetails]

      bankDetails.last4 shouldNotEqual None
      bankDetails.brand shouldNotEqual None
    }

  }

}
