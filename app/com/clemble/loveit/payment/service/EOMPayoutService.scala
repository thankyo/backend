package com.clemble.loveit.payment.service


import com.clemble.loveit.payment.model._
import com.clemble.loveit.payment.model.PayoutStatus.PayoutStatus
import play.api.libs.json._

import scala.concurrent.Future

sealed trait EOMPayoutService[BankDetails] {

  def process(payout: EOMPayout): Future[(PayoutStatus, JsValue)]

}

case object StripeEOMPayoutService extends EOMPayoutService[BankDetails] {

  override def process(payout: EOMPayout): Future[(PayoutStatus, JsValue)] = {
    Future.successful(PayoutStatus.Failed -> Json.obj())
  }

}