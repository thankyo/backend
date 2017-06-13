package com.clemble.loveit.payment

import com.clemble.loveit.common.model.{Resource, UserID}
import com.clemble.loveit.payment.model.{ChargeAccount, EOMCharge, EOMPayout, Money, StripeCustomerToken, ThankTransaction}

trait PaymentTestExecutor extends TestStripeUtils {

  def charges(user: UserID): Seq[EOMCharge]

  def payouts(user: UserID): Seq[EOMPayout]

  def getChargeAccount(user: UserID): Option[ChargeAccount]

  def addChargeAccount(user: UserID, token: StripeCustomerToken = someValidStripeToken()): ChargeAccount

  def getMonthlyLimit(user: UserID): Option[Money]

  def setMonthlyLimit(user: UserID, limit: Money): Money

  def thank(giver: UserID, owner: UserID, resource: Resource): ThankTransaction

  def pendingThanks(giver: UserID): Seq[ThankTransaction]

}
