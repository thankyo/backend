package com.clemble.loveit.payment.service

import javax.inject.{Inject, Singleton}

import com.clemble.loveit.common.model.Money
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

  val MIN_CHARGE = Money(5.0, LoveItCurrency.getInstance("USD"))

  def isUnderMin(amount: Money): Boolean = {
    amount < EOMChargeService.MIN_CHARGE
  }
}

import com.stripe.net.RequestOptions

@Singleton
case class StripeEOMChargeService @Inject()(options: RequestOptions) extends EOMChargeService {

  /**
    * Charges customer with specified charge
    *
    * @param chAcc - customer reference
    * @param amount amount to charge
    * @return StripeCharge
    */
  private def chargeStripe(chAcc: ChargeAccount, amount: Money): StripeCharge = {
    val chargeParams = Maps.newHashMap[String, Object]()
    val stripeAmount = (amount.amount * 100).toInt
    chargeParams.put("amount", stripeAmount.toString)
    chargeParams.put("currency", amount.currency.getCurrencyCode.toLowerCase())
    chargeParams.put("customer", chAcc.customer)
    StripeCharge.create(chargeParams, options)
  }

  private def doCharge(charge: EOMCharge): Future[(ChargeStatus, JsValue)] = {
    val res = Try({
      charge.account match {
        case Some(acc: ChargeAccount) => chargeStripe(acc, charge.amount)
        case None => throw new IllegalArgumentException("No ChargeAccount specified for the user")
      }
    }) match {
      case Success(charge) =>
        val details = Json.parse(charge.toJson)
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
    if (EOMChargeService.isUnderMin(charge.amount)) {
      Future.successful(ChargeStatus.UnderMin -> Json.obj())
    } else {
      doCharge(charge)
    }
  }

}

object DevEOMChargeService extends EOMChargeService {

  override def process(charge: EOMCharge): Future[(ChargeStatus, JsValue)] = {
    Future.successful(ChargeStatus.Success -> Json.obj())
  }

}
