package com.clemble.loveit.payment.service

import com.clemble.loveit.common.util.LoveItCurrency
import com.clemble.loveit.payment.model.ChargeStatus.ChargeStatus
import com.clemble.loveit.payment.model._
import com.google.common.collect.Maps
import com.stripe.model.{Charge => StripeCharge, Customer => StripeCustomer}
import play.api.libs.json.{JsValue, Json}

import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

sealed trait EOMChargeService {

  /**
    * Charges user with specified amount
    */
  def process(charge: EOMCharge): Future[(ChargeStatus, JsValue)]

}

object EOMChargeService {

  val MIN_CHARGE = Money(1.0, LoveItCurrency.getInstance("USD"))

}

case object StripeEOMChargeService extends EOMChargeService {


  /**
    * Charges customer with specified charge
    *
    * @param chAcc - customer reference
    * @param amount amount to charge
    * @return StripeCharge
    */
  private def chargeStripe(chAcc: StripeChargeAccount, amount: Money): StripeCharge = {
    val chargeParams = Maps.newHashMap[String, Object]()
    val stripeAmount = (amount.amount * 100).toInt
    chargeParams.put("amount", stripeAmount.toString)
    chargeParams.put("currency", amount.currency.getCurrencyCode.toLowerCase())
    chargeParams.put("customer", chAcc.customer)
    StripeCharge.create(chargeParams)
  }

  private def doCharge(charge: EOMCharge): Future[(ChargeStatus, JsValue)] = {
    val res = Try({
      chargeStripe(charge.source.asInstanceOf[StripeChargeAccount], charge.amount)
    }) match {
      case Success(charge) =>
        val details = Json.parse(charge.toJson())
        charge.getStatus
        (ChargeStatus.Success, details)
      case Failure(t) =>
        (ChargeStatus.Failed, Json.obj("error" -> t.getMessage))
    }
    Future.successful(res)
  }

  /**
    * Charges user with specified amount
    */
  override def process(charge: EOMCharge): Future[(ChargeStatus, JsValue)] = {
    if (charge.amount >= EOMChargeService.MIN_CHARGE) {
      doCharge(charge)
    } else {
      Future.successful(ChargeStatus.UnderMin -> Json.obj())
    }
  }

}
