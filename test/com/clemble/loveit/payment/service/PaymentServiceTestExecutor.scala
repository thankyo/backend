package com.clemble.loveit.payment.service

import com.clemble.loveit.common.ServiceSpec
import com.clemble.loveit.common.model.{Amount, Resource, UserID}
import com.clemble.loveit.payment.PaymentTestExecutor
import com.clemble.loveit.payment.model.{ChargeAccount, EOMCharge, EOMPayout, Money, StripeCustomerToken, ThankTransaction}
import com.clemble.loveit.payment.service.repository._

trait PaymentServiceTestExecutor extends ServiceSpec with PaymentTestExecutor {

  val accService = dependency[PaymentAccountService]
  val payRepo = dependency[UserPaymentRepository]
  val thankService = dependency[ThankTransactionService]
  val monLimRepo = dependency[PaymentLimitRepository]
  val eomChargeRepo = dependency[EOMChargeRepository]
  val eomPayoutRepo = dependency[EOMPayoutRepository]

  def getBalance(user: UserID): Amount = {
    val balanceOpt = await(payRepo.findById(user).map(_.map(_.balance)))
    balanceOpt.get
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

  override def setMonthlyLimit(user: UserID, limit: Money): Money = {
    await(monLimRepo.setMonthlyLimit(user, limit))
    await(monLimRepo.getMonthlyLimit(user)).get
  }

  override def thank(giver: UserID, owner: UserID, resource: Resource): ThankTransaction = {
    await(thankService.create(giver, owner, resource))
  }

  override def pendingThanks(giver: UserID): Seq[ThankTransaction] = {
    thankService.list(giver).toSeq()
  }

}
