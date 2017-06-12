package com.clemble.loveit.payment.service


import com.clemble.loveit.payment.model._
import com.clemble.loveit.payment.model.PayoutStatus.PayoutStatus
import com.google.common.collect.{ImmutableMap, Maps}
import play.api.libs.json._

import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

sealed trait EOMPayoutService[BankDetails] {

  def process(payout: EOMPayout): Future[(PayoutStatus, JsValue)]

}

import com.stripe.model.Transfer

case object StripeEOMPayoutService extends EOMPayoutService[BankDetails] {

  /**
    * Transfer specified amount to specified BankDetails
    */
  private def transferStripe(bankDetails: StripeBankDetails, amount: Money): Transfer = {
    val transferParams = Maps.newHashMap[String, Object]()
    val stripeAmount = (amount.amount * 100).toInt
    transferParams.put("amount", stripeAmount.toString)
    transferParams.put("currency", "usd")
    transferParams.put("destination", bankDetails.customer)
    Transfer.create(transferParams)
  }

  private def doProcess(payout: EOMPayout): (PayoutStatus, JsValue) = {
    Try({
      transferStripe(payout.destination.asInstanceOf[StripeBankDetails], payout.amount)
    }) match {
      case Success(transfer) =>
        val details = Json.parse(transfer.toJson())
        (PayoutStatus.Success, details)
      case Failure(t) =>
        (PayoutStatus.Failed, Json.obj("error" -> t.getMessage))
    }
  }

  override def process(payout: EOMPayout): Future[(PayoutStatus, JsValue)] = {
    if (payout.destination.isEmpty) {
      Future.successful(PayoutStatus.NoBankAccount -> Json.obj())
    } else {
      Future.successful(doProcess(payout))
    }
  }

}