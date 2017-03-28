package com.clemble.thank.payment.service

import java.util.Currency

import akka.stream.scaladsl.Source
import com.clemble.thank.model._
import com.clemble.thank.payment.model.{BankDetails, Money, PaymentTransaction}
import com.clemble.thank.payment.service.repository.PaymentTransactionRepository
import com.clemble.thank.service.{ExchangeService, UserService}
import com.google.inject.Inject

import scala.concurrent.{ExecutionContext, Future}

trait PaymentTransactionService {

  /**
    * List transactions by user
    */
  def list(user: UserID): Source[PaymentTransaction, _]

  def receive[T](user: UserID, bankDetails: BankDetails, money: Money, externalTransaction: T): Future[PaymentTransaction]

  def withdraw(user: UserID, bankDetails: BankDetails, amount: Amount, currency: Currency): Future[PaymentTransaction]

}



case class SimplePaymentTransactionService @Inject() (
                                                       userService: UserService,
                                                       exchangeService: ExchangeService,
                                                       withdrawService: WithdrawService,
                                                       repo: PaymentTransactionRepository,
                                                       implicit val ec: ExecutionContext
                                          ) extends PaymentTransactionService {


  override def list(user: UserID): Source[PaymentTransaction, _] = {
    repo.findByUser(user)
  }

  override def receive[T](user: UserID, bankDetails: BankDetails, money: Money, extTransaction: T): Future[PaymentTransaction] = {
    val thanks = exchangeService.toThanks(money)
    val transaction = PaymentTransaction.debit(user, thanks, money, bankDetails)
    for {
      userUpdate <- userService.updateBalance(user, thanks) if(userUpdate)
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
