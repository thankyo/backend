package com.clemble.loveit.payment.service

import javax.inject.{Inject, Singleton}

import com.clemble.loveit.common.model.{Amount, UserID}
import com.clemble.loveit.payment.model.{BankDetails, PaymentRequest, PaymentTransaction}
import com.clemble.loveit.payment.service.repository.PaymentTransactionRepository

import scala.concurrent.{ExecutionContext, Future}

trait PaymentService {

  def receive(user: UserID, req: PaymentRequest): Future[PaymentTransaction]

  /**
    * Receive money from external resource
    */
  def receive(transaction: PaymentTransaction): Future[PaymentTransaction]

  /**
    * Withdraw money to external system
    */
  def withdraw(user: UserID, amount: Amount): Future[PaymentTransaction]

}

@Singleton
case class SimplePaymentService @Inject()(
                                           thankBalanceService: ThankBalanceService,
                                           bankDetailsService: BankDetailsService,
                                           exchangeService: ExchangeService,
                                           withdrawService: WithdrawService[BankDetails],
                                           repo: PaymentTransactionRepository,
                                           implicit val ec: ExecutionContext
                                         ) extends PaymentService {


  override def receive(user: UserID, req: PaymentRequest): Future[PaymentTransaction] = ???

  override def receive(transaction: PaymentTransaction): Future[PaymentTransaction] = {
    for {
      _ <- bankDetailsService.set(transaction.user, transaction.source)
      userUpdate <- thankBalanceService.update(transaction.user, transaction.thanks) if (userUpdate)
      savedTransaction <- repo.save(transaction)
    } yield {
      savedTransaction
    }
  }

  override def withdraw(user: UserID, amount: Amount): Future[PaymentTransaction] = {
    val money = exchangeService.toAmount(amount)
    for {
      bankDetailsOpt <- bankDetailsService.get(user) if (bankDetailsOpt.isDefined)
      bankDetails = bankDetailsOpt.get
      withdraw <- withdrawService.withdraw(money, bankDetails) if (withdraw)
      userUpdate <- thankBalanceService.update(user, -amount) if (userUpdate)
      savedTransaction <- repo.save(PaymentTransaction.credit(user, amount, money, bankDetails))
    } yield {
      savedTransaction
    }
  }

}
