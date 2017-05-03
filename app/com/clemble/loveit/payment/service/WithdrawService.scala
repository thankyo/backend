package com.clemble.loveit.payment.service

import java.util

import com.clemble.loveit.payment.model._
import com.clemble.loveit.common.util.IDGenerator
import com.google.common.collect.Lists
import javax.inject.Singleton
import com.paypal.api.payments
import com.paypal.api.payments.{Payout, PayoutItem, PayoutSenderBatchHeader}
import com.paypal.base.rest.APIContext

import scala.concurrent.Future
import scala.util.Try

sealed trait WithdrawService[T <: BankDetails] {

  def withdraw(money: Money, account: T): Future[(String, Boolean)]

}

@Singleton
case class WithdrawServiceFacade(
                                  payPalWS: WithdrawService[PayPalBankDetails],
                                  stripeWS: WithdrawService[StripeBankDetails]
                                ) extends WithdrawService[BankDetails] {

  override def withdraw(money: Money, account: BankDetails): Future[(String, Boolean)] = {
    account match {
      case ppbd : PayPalBankDetails => payPalWS.withdraw(money, ppbd)
      case stripe: StripeBankDetails => stripeWS.withdraw(money, stripe)
      case EmptyBankDetails => Future.successful("" -> false)
    }
  }

}

object StripeWithdrawalService extends WithdrawService[StripeBankDetails] {
  override def withdraw(money: Money, account: StripeBankDetails): Future[(String, Boolean)] = {
    Future.successful(IDGenerator.generate() -> false)
  }
}

@Singleton
case class PayPalWithdrawService(context: APIContext) extends WithdrawService[PayPalBankDetails] {

  override def withdraw(money: Money, account: PayPalBankDetails): Future[(String, Boolean)] = {
    val id = IDGenerator.generate()
    val senderHeader = new PayoutSenderBatchHeader().
      setSenderBatchId(id).
      setRecipientType("EMAIL").
      setEmailSubject("LoveIt Payout <3")
    val payoutItem = toPayoutItem(money, account)
    val payout = new Payout(senderHeader, Lists.newArrayList(payoutItem))
    val result = Try(payout.create(context, new util.HashMap[String, String]()))
    Future.successful(id -> result.isSuccess)
  }

  private def toPayoutItem(money: Money, account: PayPalBankDetails): PayoutItem = {
    new PayoutItem().
      setReceiver(account.email).
      setRecipientType("EMAIL").
      setAmount(toPayPalCurrency(money)).
      setNote("LoveIt Payout <3")
  }

  private def toPayPalCurrency(money: Money) = {
    import money._
    new payments.Currency(currency.getCurrencyCode, amount.toString())
  }

}