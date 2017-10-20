package com.clemble.loveit.payment.service

import javax.inject.{Inject, Singleton}

import akka.actor.{Actor, ActorSystem, Props}
import akka.stream.scaladsl.Source
import com.clemble.loveit.common.model.{Resource, ThankTransaction, UserID}
import com.clemble.loveit.payment.model.PendingTransaction
import com.clemble.loveit.payment.service.repository.{PendingTransactionRepository, UserBalanceRepository}
import com.clemble.loveit.thank.service.ThankEventBus
import com.mohiva.play.silhouette.api.Logger

import scala.concurrent.{ExecutionContext, Future}

trait PendingTransactionService {

  def list(user: UserID): Source[PendingTransaction, _]

  def create(giver: UserID, owner: UserID, url: Resource): Future[PendingTransaction]

  def removeAll(user: UserID, thank: Seq[PendingTransaction]): Future[Boolean]
}

case class PaymentThankListener(service: PendingTransactionService) extends Actor {
  override def receive = {
    case ThankTransaction(giver, owner, res, _) =>
      service.create(giver, owner, res)
  }
}

@Singleton
case class SimplePendingTransactionService @Inject()(
                                                    actorSystem: ActorSystem,
                                                    thankEventBus: ThankEventBus,
                                                    balanceRepo: UserBalanceRepository,
                                                    repository: PendingTransactionRepository,
                                                    implicit val ec: ExecutionContext
                                                  ) extends PendingTransactionService with Logger {

  {
    val subscriber = actorSystem.actorOf(Props(PaymentThankListener(this)))
    thankEventBus.subscribe(subscriber, classOf[ThankTransaction])
  }

  override def list(user: UserID): Source[PendingTransaction, _] = {
    repository.findByUser(user)
  }

  override def create(giver: UserID, owner: UserID, url: Resource): Future[PendingTransaction] = {
    val transaction = PendingTransaction(owner, url)
    for {
      savedInRepo <- repository.save(giver, transaction)
      updatedOwner <- balanceRepo.updateBalance(owner, 1) if (savedInRepo)
      updatedGiver <- balanceRepo.updateBalance(giver, -1) if (savedInRepo)
    } yield {
      if (!updatedGiver || !updatedOwner || !savedInRepo)
        logger.error(s"${giver} ${owner} ${url} failed to properly process transaction ${updatedGiver} ${updatedOwner} ${savedInRepo}")
      transaction
    }
  }

  override def removeAll(giver: UserID, transactions: Seq[PendingTransaction]): Future[Boolean] = {
    repository.removeAll(giver, transactions)
  }

}