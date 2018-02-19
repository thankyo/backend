package com.clemble.loveit.payment.service

import com.clemble.loveit.common.ServiceSpec
import com.clemble.loveit.common.model._
import com.clemble.loveit.payment.PaymentTestExecutor
import com.clemble.loveit.payment.model.{ChargeAccount, EOMCharge, EOMPayout, PendingTransaction, StripeCustomerToken}
import com.clemble.loveit.payment.service.repository._
import com.clemble.loveit.thank.model.Project
import com.clemble.loveit.dev.service.DevStripeUtils._

trait PaymentServiceTestExecutor extends ServiceSpec with PaymentTestExecutor {

  val accService = dependency[ChargeAccountService]
  val payRepo = dependency[PaymentRepository]
  val thankTransactionService = dependency[PendingTransactionService]
  val monLimRepo = dependency[PaymentLimitRepository]
  val eomChargeRepo = dependency[EOMChargeRepository]
  val eomPayoutRepo = dependency[EOMPayoutRepository]

  override def charges(user: UserID): Seq[EOMCharge] = {
    await(eomChargeRepo.findByUser(user))
  }

  override def payouts(user: UserID): Seq[EOMPayout] = {
    await(eomPayoutRepo.findByUser(user))
  }

  override def getChargeAccount(user: UserID): Option[ChargeAccount] = {
    await(accService.getChargeAccount(user))
  }

  override def addChargeAccount(user: UserID, token: StripeCustomerToken = someValidStripeToken()): ChargeAccount = {
    await(accService.updateChargeAccount(user, token))
  }

  override def getMonthlyLimit(user: UserID): Option[Money] = {
    await(monLimRepo.getMonthlyLimit(user))
  }

  override def setMonthlyLimit(user: UserID, limit: Money): Boolean = {
    await(monLimRepo.setMonthlyLimit(user, limit))
  }

  override def thank(giver: UserID, project: Project, resource: Resource): PendingTransaction = {
    await(thankTransactionService.create(giver, project, resource))
  }

  override def pendingCharges(giver: UserID): Seq[PendingTransaction] = {
    await(thankTransactionService.listCharges(giver))
  }

}
