package com.clemble.loveit.payment.service

import javax.inject.{Inject, Singleton}

import com.clemble.loveit.common.model.{Amount, UserID}
import com.clemble.loveit.payment.model.{BankDetails, PaymentRequest, PaymentTransaction}
import com.clemble.loveit.payment.service.repository.PaymentTransactionRepository

import scala.concurrent.{ExecutionContext, Future}

trait PaymentService {

  /**
    * Receive money from external resource
    */
  def receive(user: UserID, req: PaymentRequest): Future[PaymentTransaction]

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
                                           processingService: PaymentProcessingService[PaymentRequest],
                                           withdrawService: WithdrawService[BankDetails],
                                           repo: PaymentTransactionRepository,
                                           implicit val ec: ExecutionContext
                                         ) extends PaymentService {


  override def receive(user: UserID, req: PaymentRequest): Future[PaymentTransaction] = {
    for {
      (id, bankDetails, money) <- processingService.process(req)
      thanks = exchangeService.toThanks(money)
      _ <- bankDetailsService.set(user, bankDetails)
      userUpdate <- thankBalanceService.update(user, thanks) if (userUpdate)
      debitTransaction = PaymentTransaction.debit(id, user, thanks, money, bankDetails)
      savedTransaction <- repo.save(debitTransaction)
    } yield {
      savedTransaction
    }
  }

  override def withdraw(user: UserID, amount: Amount): Future[PaymentTransaction] = {
    val money = exchangeService.toAmount(amount)
    for {
      bankDetailsOpt <- bankDetailsService.get(user) if (bankDetailsOpt.isDefined)
      bankDetails = bankDetailsOpt.get
      (id, withdraw) <- withdrawService.withdraw(money, bankDetails) if (withdraw)
      userUpdate <- thankBalanceService.update(user, -amount) if (userUpdate)
      creditTransaction = PaymentTransaction.credit(id, user, amount, money, bankDetails)
      savedTransaction <- repo.save(creditTransaction)
    } yield {
      savedTransaction
    }
  }

}
