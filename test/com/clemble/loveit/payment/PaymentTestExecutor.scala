package com.clemble.loveit.payment

import com.clemble.loveit.common.model.{Resource, UserID}
import com.clemble.loveit.dev.service.DevStripeUtils._
import com.clemble.loveit.payment.model.{ChargeAccount, EOMCharge, EOMPayout, PendingTransaction, StripeCustomerToken}
import com.clemble.loveit.payment.service.repository.PaymentLimitExecutor
import com.clemble.loveit.thank.model.Project

trait PaymentTestExecutor extends PaymentLimitExecutor {

  def charges(user: UserID): Seq[EOMCharge]

  def payouts(user: UserID): Seq[EOMPayout]

  def getChargeAccount(user: UserID): Option[ChargeAccount]

  def addChargeAccount(user: UserID, token: StripeCustomerToken = someValidStripeToken()): ChargeAccount

  def thank(giver: UserID, project: Project): PendingTransaction = {
    thank(giver, project, project.url)
  }

  def thank(giver: UserID, project: Project, url: Resource): PendingTransaction

  def pendingCharges(giver: UserID): Seq[PendingTransaction]

}
