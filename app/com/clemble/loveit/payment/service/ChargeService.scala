package com.clemble.loveit.payment.service

import javax.inject.{Inject, Singleton}

import com.clemble.loveit.common.model.{Amount, UserID}
import com.clemble.loveit.payment.model.{BankDetails, Charge, PaymentRequest, UserPayment}
import com.clemble.loveit.payment.service.repository.{PaymentRepository, PaymentTransactionRepository}

import scala.concurrent.{ExecutionContext, Future}

sealed trait ChargeService {

  /**
    * Charges user with
    */
  def charge(user: UserPayment): Future[Charge]

//  /**
//    * Withdraw money to external system
//    */
//  def withdraw(user: UserID, amount: Amount): Future[Charge]

}
//
//@Singleton
//case class SimpleChargeService @Inject()(
//                                           paymentRepo: PaymentRepository,
//                                           exchangeService: ExchangeService,
//                                           processingService: PaymentProcessingService[PaymentRequest],
//                                           withdrawService: WithdrawService[BankDetails],
//                                           repo: PaymentTransactionRepository,
//                                           implicit val ec: ExecutionContext
//                                         ) extends ChargeService {
//
//
//  override def charge(user: UserID, req: PaymentRequest): Future[Charge] = {
//    for {
//      (id, bankDetails, money) <- processingService.process(req)
//      thanks = exchangeService.toThanks(money)
//      _ <- paymentRepo.setBankDetails(user, bankDetails)
//      userUpdate <- paymentRepo.updateBalance(user, thanks) if (userUpdate)
//      debitTransaction = Charge(id, user, thanks, money, bankDetails)
//      savedTransaction <- repo.save(debitTransaction)
//    } yield {
//      savedTransaction
//    }
//  }
//
//  override def withdraw(user: UserID, amount: Amount): Future[Charge] = {
//    val money = exchangeService.toAmount(amount)
//    for {
//      bankDetailsOpt <- paymentRepo.getBankDetails(user) if (bankDetailsOpt.isDefined)
//      bankDetails = bankDetailsOpt.get
//      (id, withdraw) <- withdrawService.withdraw(money, bankDetails) if (withdraw)
//      userUpdate <- paymentRepo.updateBalance(user, -amount) if (userUpdate)
//      creditTransaction = Charge.credit(id, user, amount, money, bankDetails)
//      savedTransaction <- repo.save(creditTransaction)
//    } yield {
//      savedTransaction
//    }
//  }
//
//}
