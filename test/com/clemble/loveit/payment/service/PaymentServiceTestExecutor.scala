package com.clemble.loveit.payment.service

import com.clemble.loveit.common.ServiceSpec
import com.clemble.loveit.common.model._
import com.clemble.loveit.payment.PaymentTestExecutor
import com.clemble.loveit.payment.model.{ChargeAccount, EOMCharge, EOMPayout, PendingTransaction, StripeCustomerToken}
import com.clemble.loveit.payment.service.repository._
import com.clemble.loveit.thank.service.repository.SupportedProjectRepository

trait PaymentServiceTestExecutor extends ServiceSpec with PaymentTestExecutor {

  val accService = dependency[ChargeAccountService]
  val payRepo = dependency[PaymentRepository]
  val thankTransactionService = dependency[PendingTransactionService]
  val monLimRepo = dependency[PaymentLimitRepository]
  val eomChargeRepo = dependency[EOMChargeRepository]
  val eomPayoutRepo = dependency[EOMPayoutRepository]
  val projectsRepo = dependency[SupportedProjectRepository]

  def getBalance(user: UserID): Amount = {
    val userOpt = await(payRepo.findById(user))
    userOpt.map(_.balance).get
  }

  override def charges(user: UserID): Seq[EOMCharge] = {
    eomChargeRepo.findByUser(user).toSeq
  }

  override def payouts(user: UserID): Seq[EOMPayout] = {
    eomPayoutRepo.findByUser(user).toSeq()
  }

  override def getChargeAccount(user: UserID): Option[ChargeAccount] = {
    await(accService.getChargeAccount(user))
  }

  override def addChargeAccount(user: UserID, token: StripeCustomerToken): ChargeAccount = {
    await(accService.updateChargeAccount(user, token))
  }

  override def getMonthlyLimit(user: UserID): Option[Money] = {
    await(monLimRepo.getMonthlyLimit(user))
  }

  override def setMonthlyLimit(user: UserID, limit: Money): Boolean = {
    await(monLimRepo.setMonthlyLimit(user, limit))
  }

  override def thank(giver: UserID, owner: UserID, resource: Resource): PendingTransaction = {
    val projectOpt = await(projectsRepo.getProject(owner))
    await(thankTransactionService.create(giver, projectOpt.get, resource))
  }

  override def pendingThanks(giver: UserID): Seq[PendingTransaction] = {
    thankTransactionService.list(giver).toSeq()
  }

}
