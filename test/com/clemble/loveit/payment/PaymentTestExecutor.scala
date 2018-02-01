package com.clemble.loveit.payment

import com.clemble.loveit.common.model.{Amount, Resource, UserID}
import com.clemble.loveit.payment.model.{ChargeAccount, EOMCharge, EOMPayout, PendingTransaction, StripeCustomerToken}
import com.clemble.loveit.payment.service.repository.PaymentLimitExecutor
import com.clemble.loveit.thank.model.SupportedProject

trait PaymentTestExecutor extends TestStripeUtils with PaymentLimitExecutor {

  def getBalance(user: UserID): Amount

  def charges(user: UserID): Seq[EOMCharge]

  def payouts(user: UserID): Seq[EOMPayout]

  def getChargeAccount(user: UserID): Option[ChargeAccount]

  def addChargeAccount(user: UserID, token: StripeCustomerToken = someValidStripeToken()): ChargeAccount

  def thank(giver: UserID, project: SupportedProject): PendingTransaction = {
    thank(giver, project, project.resource)
  }

  def thank(giver: UserID, project: SupportedProject, resource: Resource): PendingTransaction

  def pendingThanks(giver: UserID): Seq[PendingTransaction]

}
