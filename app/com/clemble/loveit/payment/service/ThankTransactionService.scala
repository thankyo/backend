package com.clemble.loveit.payment.service

import akka.stream.scaladsl.Source
import com.clemble.loveit.common.model.{Amount, Resource, UserID}
import com.clemble.loveit.payment.model.ThankTransaction
import com.clemble.loveit.payment.service.repository.ThankTransactionRepository
import com.clemble.loveit.thank.service.ResourceOwnershipService
import javax.inject.{Inject, Singleton}

import scala.concurrent.{ExecutionContext, Future}

trait ThankTransactionService {

  def list(user: UserID): Source[ThankTransaction, _]

  def create(giver: UserID, url: Resource, amount: Amount): Future[List[ThankTransaction]]

}

@Singleton
case class SimpleThankTransactionService @Inject()(ownershipService: ResourceOwnershipService, repository: ThankTransactionRepository, implicit val ec: ExecutionContext) extends ThankTransactionService {

  override def list(user: UserID): Source[ThankTransaction, _] = {
    repository.findByUser(user)
  }

  override def create(giverId: UserID, url: Resource, amount: Amount): Future[List[ThankTransaction]] = {
    for {
      owner <- ownershipService.findResourceOwner(url)
      giverCreditOp <- credit(giverId,url,  amount)
      ownerDebitOp <- debit(owner.id, url, amount)
    } yield {
      List(ownerDebitOp, giverCreditOp)
    }
  }

  private def debit(user: UserID, uri: Resource, amount: Amount): Future[ThankTransaction] = {
    val debitOperation = ThankTransaction.debit(user, uri, amount)
    for {
      _ <- ownershipService.updateBalance(user, amount)
      payment <- repository.save(debitOperation)
    } yield {
      payment
    }
  }

  private def credit(user: UserID, uri: Resource, amount: Amount): Future[ThankTransaction] = {
    val creditOperation = ThankTransaction.credit(user, uri, amount)
    for {
      _ <- ownershipService.updateBalance(user, -amount)
      payment <- repository.save(creditOperation)
    } yield {
      payment
    }
  }

}