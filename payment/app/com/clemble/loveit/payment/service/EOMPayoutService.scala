package com.clemble.loveit.payment.service


import java.time.YearMonth
import javax.inject.{Inject, Singleton}

import com.clemble.loveit.common.model.{Money, UserID}
import com.clemble.loveit.payment.model._
import com.clemble.loveit.payment.model.PayoutStatus.PayoutStatus
import com.clemble.loveit.payment.service.repository.EOMPayoutRepository
import com.google.common.collect.Maps
import play.api.libs.json._

import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

sealed trait EOMPayoutService {

  def findByUser(user: UserID): Future[List[EOMPayout]]

  def process(payout: EOMPayout): Future[(PayoutStatus, JsValue)]

}


@Singleton
class SimpleEOMPayoutService @Inject()(repo: EOMPayoutRepository, payoutAccService: PayoutAccountService, processor: EOMPayoutProcessor) extends EOMPayoutService {

  override def findByUser(user: UserID): Future[List[EOMPayout]] = {
    repo.findByUser(user)
  }

  override def process(payout: EOMPayout): Future[(PayoutStatus, JsValue)] = {
    processor.process(payout)
  }

}



sealed trait EOMPayoutProcessor {

  def process(payout: EOMPayout): Future[(PayoutStatus, JsValue )]

}

import com.stripe.model.Transfer

case object StripeEOMPayoutProcessor extends EOMPayoutProcessor {

  /**
    * Transfer specified amount to specified [[ChargeAccount]]
    */
  private def transferStripe(chAcc: PayoutAccount, amount: Money): Transfer = {
    val transferParams = Maps.newHashMap[String, Object]()
    val stripeAmount = (amount.amount * 100).toInt
    transferParams.put("amount", stripeAmount.toString)
    transferParams.put("currency", "usd")
    transferParams.put("destination", chAcc.accountId)
    Transfer.create(transferParams)
  }

  private def doProcess(payout: EOMPayout): (PayoutStatus, JsValue) = {
    Try({
      payout.destination match {
        case Some(account) =>
          transferStripe(account, payout.amount)
        case None =>
          throw new IllegalArgumentException("No Payout account specified for the client")
      }
    }) match {
      case Success(transfer) =>
        val details = Json.parse(transfer.toJson)
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

case object DevEOMPayoutProcessor extends EOMPayoutProcessor {

  override def process(payout: EOMPayout): Future[(PayoutStatus, JsValue)] = {
    Future.successful(PayoutStatus.Success -> Json.obj())
  }

}