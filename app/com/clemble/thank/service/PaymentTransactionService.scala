package com.clemble.thank.service

import java.util.Currency

import com.clemble.thank.model._
import com.clemble.thank.service.repository.PaymentTransactionRepository
import com.google.inject.Inject

import scala.concurrent.{ExecutionContext, Future}

trait PaymentTransactionService {

  def receive(user: UserID, bankDetails: BankDetails, amount: Money): Future[PaymentTransaction]

  def withdraw(user: UserID, bankDetails: BankDetails, amount: Money): Future[PaymentTransaction]

}



case class SimplePaymentTransactionService @Inject() (
                                            userService: UserService,
                                            exchangeService: ExchangeService,
                                            transactionRepository: PaymentTransactionRepository,
                                            implicit val ec: ExecutionContext
                                          ) extends PaymentTransactionService {

  override def receive(user: UserID, bankDetails: BankDetails, amount: Money): Future[PaymentTransaction] = {
    val thanks = exchangeService.toThanks(amount)
    val transaction = PaymentTransaction.debit(user, thanks, amount, bankDetails)
    for {
      userUpdate <- userService.updateBalance(user, thanks) if(userUpdate)
      savedTransaction <- transactionRepository.save(transaction)
    } yield {
      savedTransaction
    }
  }

  override def withdraw(user: UserID, bankDetails: BankDetails, amount: Money): Future[PaymentTransaction] = ???

}
