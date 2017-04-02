package com.clemble.loveit.payment.service

import java.util

import com.clemble.loveit.payment.model.{BankDetails, EmptyBankDetails, Money, PayPalBankDetails}
import com.clemble.loveit.util.IDGenerator
import com.google.common.collect.Lists
import com.paypal.api.payments.{Payout, PayoutItem, PayoutSenderBatchHeader}
import com.paypal.base.rest.APIContext

import scala.concurrent.Future
import scala.util.Try

trait WithdrawService[T <: BankDetails] {

  def withdraw(money: Money, account: T): Future[Boolean]

}

case class WithdrawServiceFacade(payPalWS: WithdrawService[PayPalBankDetails]) extends WithdrawService[BankDetails] {

  override def withdraw(money: Money, account: BankDetails): Future[Boolean] = {
    account match {
      case ppbd : PayPalBankDetails => payPalWS.withdraw(money, ppbd)
      case EmptyBankDetails => Future.successful(false)
    }
  }

}

case class PayPalWithdrawService(context: APIContext) extends WithdrawService[PayPalBankDetails] {

  override def withdraw(money: Money, account: PayPalBankDetails): Future[Boolean] = {
    val senderHeader = new PayoutSenderBatchHeader().
      setSenderBatchId(IDGenerator.generate()).
      setRecipientType("EMAIL").
      setEmailSubject("LoveIt Payout <3")
    val payoutItem = toPayoutItem(money, account)
    val payout = new Payout(senderHeader, Lists.newArrayList(payoutItem))
    val result = Try(payout.create(context, new util.HashMap[String, String]()))
    Future.successful(result.isSuccess)
  }

  private def toPayoutItem(money: Money, account: PayPalBankDetails): PayoutItem = {
    new PayoutItem().
      setReceiver(account.email).
      setRecipientType("EMAIL").
      setAmount(money.toPayPalCurrency()).
      setNote("LoveIt Payout <3")
  }

}