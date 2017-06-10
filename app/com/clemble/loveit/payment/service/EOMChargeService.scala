package com.clemble.loveit.payment.service

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

case object StripeEOMChargeService extends EOMChargeService {

  /**
    * Charges customer with specified charge
    *
    * @param bankDetails - customer reference
    * @param amount amount to charge
    * @return StripeCharge
    */
  private def chargeStripe(bankDetails: StripeBankDetails, amount: Money): StripeCharge = {
    val chargeParams = Maps.newHashMap[String, Object]()
    val stripeAmount = (amount.amount * 100).toInt
    chargeParams.put("amount", stripeAmount.toString)
    chargeParams.put("currency", amount.currency.getCurrencyCode.toLowerCase())
    chargeParams.put("customer", bankDetails.customer)
    StripeCharge.create(chargeParams)
  }

  private def doCharge(charge: EOMCharge): Future[(ChargeStatus, JsValue)] = {
    val res = Try({
      chargeStripe(charge.source.asInstanceOf[StripeBankDetails], charge.amount)
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
    if (charge.moreThanMinCharge) {
      doCharge(charge)
    } else {
      Future.successful(ChargeStatus.UnderMin -> Json.obj())
    }
  }

}
