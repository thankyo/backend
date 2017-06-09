package com.clemble.loveit.payment.service

import akka.stream.scaladsl.Source
import com.clemble.loveit.common.model.{Resource, UserID}
import com.clemble.loveit.payment.model.ThankTransaction
import com.clemble.loveit.payment.service.repository.{BalanceRepository, ThankTransactionRepository}
import javax.inject.{Inject, Singleton}

import com.mohiva.play.silhouette.api.Logger

import scala.concurrent.{ExecutionContext, Future}

trait ThankTransactionService {

  def list(user: UserID): Source[ThankTransaction, _]

  def create(giver: UserID, owner: UserID, url: Resource): Future[ThankTransaction]

}

@Singleton
case class SimpleThankTransactionService @Inject()(ownershipService: BalanceRepository, repository: ThankTransactionRepository, implicit val ec: ExecutionContext) extends ThankTransactionService with Logger {

  override def list(user: UserID): Source[ThankTransaction, _] = {
    repository.findByUser(user)
  }

  override def create(giver: UserID, owner: UserID, url: Resource): Future[ThankTransaction] = {
    val transaction = ThankTransaction(giver, owner, url)
    for {
      savedInRepo <- repository.save(transaction)
      updatedOwner <- ownershipService.updateBalance(owner, 1) if (savedInRepo)
      updatedGiver <- ownershipService.updateBalance(giver, -1) if (savedInRepo)
    } yield {
      if (!updatedGiver || !updatedOwner || !savedInRepo)
        logger.error(s"${giver} ${owner} ${url} failed to properly process transaction ${updatedGiver} ${updatedOwner} ${savedInRepo}")
      transaction
    }
  }

}