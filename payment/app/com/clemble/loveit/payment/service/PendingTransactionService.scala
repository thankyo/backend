package com.clemble.loveit.payment.service

import javax.inject.{Inject, Singleton}

import akka.actor.{Actor, ActorSystem, Props}
import akka.stream.scaladsl.Source
import com.clemble.loveit.common.model.{Resource, ThankEvent, UserID}
import com.clemble.loveit.payment.model.PendingTransaction
import com.clemble.loveit.payment.service.repository.{PendingTransactionRepository, UserBalanceRepository}
import com.clemble.loveit.thank.model.Project
import com.clemble.loveit.thank.service.ThankEventBus
import com.mohiva.play.silhouette.api.Logger

import scala.concurrent.{ExecutionContext, Future}

trait PendingTransactionService {

  def list(user: UserID): Source[PendingTransaction, _]

  def create(giver: UserID, owner: Project, url: Resource): Future[PendingTransaction]

  def removeAll(user: UserID, thank: Seq[PendingTransaction]): Future[Boolean]
}

case class PaymentThankListener(service: PendingTransactionService) extends Actor {
  override def receive = {
    case ThankEvent(giver, owner, res, _) =>
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
    thankEventBus.subscribe(subscriber, classOf[ThankEvent])
  }

  override def list(user: UserID): Source[PendingTransaction, _] = {
    repository.findByUser(user)
  }

  override def create(giver: UserID, project: Project, url: Resource): Future[PendingTransaction] = {
    val transaction = PendingTransaction(project, url)
    for {
      savedInRepo <- repository.save(giver, transaction)
      updatedOwner <- balanceRepo.updateBalance(project.user, 1) if (savedInRepo)
      updatedGiver <- balanceRepo.updateBalance(giver, -1) if (savedInRepo)
    } yield {
      if (!updatedGiver || !updatedOwner || !savedInRepo)
        logger.error(s"${giver} ${project.user} ${url} failed to properly process transaction ${updatedGiver} ${updatedOwner} ${savedInRepo}")
      transaction
    }
  }

  override def removeAll(giver: UserID, transactions: Seq[PendingTransaction]): Future[Boolean] = {
    repository.removeAll(giver, transactions)
  }

}