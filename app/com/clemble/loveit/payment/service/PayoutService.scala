package com.clemble.loveit.payment.service


import com.clemble.loveit.payment.model._
import com.clemble.loveit.payment.model.PayoutStatus.PayoutStatus
import play.api.libs.json._

import scala.concurrent.Future

sealed trait PayoutService[BankDetails] {

  def process(payout: Payout): Future[(PayoutStatus, JsValue)]

}

case object StripePayoutService extends PayoutService[BankDetails] {

  override def process(payout: Payout): Future[(PayoutStatus, JsValue)] = {
    Future.successful(PayoutStatus.Failed -> Json.obj())
  }

}