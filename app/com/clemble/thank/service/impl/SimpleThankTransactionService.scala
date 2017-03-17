package com.clemble.thank.service.impl

import akka.stream.scaladsl.Source
import com.clemble.thank.model._
import com.clemble.thank.service.repository.ThankTransactionRepository
import com.clemble.thank.service.{ThankTransactionService, UserService}
import com.google.inject.{Inject, Singleton}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
case class SimpleThankTransactionService @Inject()(userService: UserService, repository: ThankTransactionRepository, implicit val ec: ExecutionContext) extends ThankTransactionService {

  override def list(user: UserID): Source[ThankTransaction, _] = {
    repository.findByUser(user)
  }

  override def create(giverId: UserID, url: Resource, amount: Amount): Future[List[ThankTransaction]] = {
    for {
      owner <- userService.findResourceOwner(url)
      ownerDebitOp <- debit(owner.id, url, amount)
      giverCreditOp <- credit(giverId,url,  amount)
    } yield {
      List(ownerDebitOp, giverCreditOp)
    }
  }

  private def debit(user: UserID, uri: Resource, amount: Amount): Future[ThankTransaction] = {
    val debitOperation = ThankTransaction.debit(user, uri, amount)
    for {
      _ <- userService.updateBalance(user, amount)
      payment <- repository.save(debitOperation)
    } yield {
      payment
    }
  }

  private def credit(user: UserID, uri: Resource, amount: Amount): Future[ThankTransaction] = {
    val creditOperation = ThankTransaction.credit(user, uri, amount)
    for {
      _ <- userService.updateBalance(user, -amount)
      payment <- repository.save(creditOperation)
    } yield {
      payment
    }
  }

}
