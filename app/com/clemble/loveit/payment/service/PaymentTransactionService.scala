package com.clemble.loveit.payment.service

import java.util.Currency

import akka.stream.scaladsl.Source
import com.clemble.loveit.common.model.{Amount, UserID}
import com.clemble.loveit.payment.model.{BankDetails, PaymentTransaction}
import com.clemble.loveit.payment.service.repository.PaymentTransactionRepository
import com.clemble.loveit.user.service.UserService
import com.google.inject.Inject

import scala.concurrent.{ExecutionContext, Future}

trait PaymentTransactionService {

  /**
    * List transactions by user
    */
  def list(user: UserID): Source[PaymentTransaction, _]

  /**
    * Receive money from external resource
    */
  def receive(transaction: PaymentTransaction): Future[PaymentTransaction]

  /**
    * Withdraw money to external system
    */
  def withdraw(user: UserID, bankDetails: BankDetails, amount: Amount, currency: Currency): Future[PaymentTransaction]

}



case class SimplePaymentTransactionService @Inject() (
                                                       userService: UserService,
                                                       exchangeService: ExchangeService,
                                                       withdrawService: WithdrawService[BankDetails],
                                                       repo: PaymentTransactionRepository,
                                                       implicit val ec: ExecutionContext
                                          ) extends PaymentTransactionService {


  override def list(user: UserID): Source[PaymentTransaction, _] = {
    repo.findByUser(user)
  }

  override def receive(transaction: PaymentTransaction): Future[PaymentTransaction] = {
    for {
      _ <- userService.setBankDetails(transaction.user, transaction.source)
      userUpdate <- userService.updateBalance(transaction.user, transaction.thanks) if(userUpdate)
      savedTransaction <- repo.save(transaction)
    } yield {
      savedTransaction
    }
  }

  override def withdraw(user: UserID, bankDetails: BankDetails, amount: Amount, currency: Currency): Future[PaymentTransaction] = {
    val money = exchangeService.toAmount(amount, currency)
    val transaction = PaymentTransaction.credit(user, amount, money, bankDetails)
    for {
      withdraw <- withdrawService.withdraw(money, bankDetails) if (withdraw)
      userUpdate <- userService.updateBalance(user, - amount) if (userUpdate)
      savedTransaction <- repo.save(transaction)
    } yield {
      savedTransaction
    }
  }

}
