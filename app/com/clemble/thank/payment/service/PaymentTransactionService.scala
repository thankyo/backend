package com.clemble.thank.payment.service

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

  def receive[T](user: UserID, bankDetails: BankDetails, amount: Money, externalTransaction: T): Future[PaymentTransaction]

  def withdraw(user: UserID, bankDetails: BankDetails, amount: Money): Future[PaymentTransaction]

}



case class SimplePaymentTransactionService @Inject() (
                                                       userService: UserService,
                                                       exchangeService: ExchangeService,
                                                       repo: PaymentTransactionRepository,
                                                       implicit val ec: ExecutionContext
                                          ) extends PaymentTransactionService {


  override def list(user: UserID): Source[PaymentTransaction, _] = {
    repo.findByUser(user)
  }

  override def receive[T](user: UserID, bankDetails: BankDetails, amount: Money, extTransaction: T): Future[PaymentTransaction] = {
    val thanks = exchangeService.toThanks(amount)
    val transaction = PaymentTransaction.debit(user, thanks, amount, bankDetails)
    for {
      userUpdate <- userService.updateBalance(user, thanks) if(userUpdate)
      savedTransaction <- repo.save(transaction)
    } yield {
      savedTransaction
    }
  }

  override def withdraw(user: UserID, bankDetails: BankDetails, amount: Money): Future[PaymentTransaction] = ???

}
