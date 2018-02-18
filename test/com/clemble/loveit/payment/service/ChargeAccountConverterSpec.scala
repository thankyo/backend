package com.clemble.loveit.payment.service

import com.clemble.loveit.common.ServiceSpec
import com.clemble.loveit.dev.service.DevStripeUtils._

import scala.util.Try

class ChargeAccountConverterSpec extends ServiceSpec {

  val service: ChargeAccountConverter = dependency[ChargeAccountConverter]

  "STRIPE" should {

    "create bank details" in {
      val token = someValidStripeToken()

      val chAcc = Try(await(service.processChargeToken(token)))

      chAcc.isSuccess shouldEqual true
    }

    "specify card brand  and last4 digits" in {
      val token = someValidStripeToken()

      val chAcc = await(service.processChargeToken(token))

      chAcc.last4 shouldNotEqual None
      chAcc.brand shouldNotEqual None
    }

  }

}
