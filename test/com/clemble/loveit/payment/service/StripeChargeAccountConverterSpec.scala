package com.clemble.loveit.payment.service

import com.clemble.loveit.common.ServiceSpec
import com.clemble.loveit.payment.model.StripeChargeAccount

import scala.util.Try

class StripeChargeAccountConverterSpec extends ServiceSpec with TestStripeUtils{

  val service = dependency[ChargeAccountConverter]

  "STRIPE" should {

    "create bank details" in {
      val token = someValidStripeToken()

      val chAcc = Try(await(service.processChargeToken(token)))

      chAcc.isSuccess shouldEqual true
    }

    "specify card brand  and last4 digits" in {
      val token = someValidStripeToken()

      val chAcc = await(service.processChargeToken(token)).asInstanceOf[StripeChargeAccount]

      chAcc.last4 shouldNotEqual None
      chAcc.brand shouldNotEqual None
    }

  }

}
