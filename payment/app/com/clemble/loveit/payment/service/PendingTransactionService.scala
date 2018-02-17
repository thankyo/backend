package com.clemble.loveit.payment.service

import javax.inject.{Inject, Singleton}

import akka.actor.{Actor, ActorSystem, Props}
import com.clemble.loveit.common.model.{Resource, ThankEvent, UserID}
import com.clemble.loveit.payment.model.PendingTransaction
import com.clemble.loveit.payment.service.repository.{PendingTransactionRepository}
import com.clemble.loveit.thank.model.Project
import com.clemble.loveit.thank.service.ThankEventBus
import com.mohiva.play.silhouette.api.Logger

import scala.concurrent.{ExecutionContext, Future}

trait PendingTransactionService {

  def listOutgoing(user: UserID): Future[List[PendingTransaction]]

  def listIncoming(user: UserID): Future[List[PendingTransaction]]

  def create(giver: UserID, owner: Project, url: Resource): Future[PendingTransaction]

  def removeOutgoing(user: UserID, thank: Seq[PendingTransaction]): Future[Boolean]

  def removeIncoming(user: UserID, transactions: Seq[PendingTransaction]): Future[Boolean]
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
                                                      repo: PendingTransactionRepository,
                                                      implicit val ec: ExecutionContext
                                                  ) extends PendingTransactionService with Logger {

  {
    val subscriber = actorSystem.actorOf(Props(PaymentThankListener(this)))
    thankEventBus.subscribe(subscriber, classOf[ThankEvent])
  }

  override def listOutgoing(user: UserID): Future[List[PendingTransaction]] = {
    repo.findOutgoingByUser(user)
  }

  override def listIncoming(user: UserID): Future[List[PendingTransaction]] = {
    repo.findIncomingByUser(user)
  }

  override def create(giver: UserID, project: Project, url: Resource): Future[PendingTransaction] = {
    val transaction = PendingTransaction(project, url)
    for {
      savedInRepo <- repo.save(giver, transaction)
    } yield {
      if (!savedInRepo)
        logger.error(s"${giver} ${project.user} ${url} failed to properly process transaction ${savedInRepo}")
      transaction
    }
  }

  override def removeOutgoing(giver: UserID, transactions: Seq[PendingTransaction]): Future[Boolean] = {
    repo.removeOutgoing(giver, transactions)
  }

  override def removeIncoming(user: UserID, transactions: Seq[PendingTransaction]): Future[Boolean] = {
    repo.removeOutgoing(user, transactions)
  }
}