package com.clemble.loveit.payment.service

import javax.inject.{Inject, Singleton}
import akka.actor.{Actor, ActorSystem, Props}
import com.clemble.loveit.common.model.{Project, Resource, ThankEvent, UserID}
import com.clemble.loveit.common.service.ThankEventBus
import com.clemble.loveit.payment.model.PendingTransaction
import com.clemble.loveit.payment.service.repository.PendingTransactionRepository
import com.mohiva.play.silhouette.api.Logger

import scala.concurrent.{ExecutionContext, Future}

trait PendingTransactionService {

  def listCharges(user: UserID): Future[List[PendingTransaction]]

  def listPayouts(user: UserID): Future[List[PendingTransaction]]

  def create(giver: UserID, owner: Project, url: Resource): Future[PendingTransaction]

  def findUsersWithPayouts(): Future[List[UserID]]

  def findUsersWithoutCharges(): Future[List[UserID]]

  def removeCharges(user: UserID, thank: Seq[PendingTransaction]): Future[Boolean]

  def removePayouts(user: UserID, transactions: Seq[PendingTransaction]): Future[Boolean]
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

  override def listCharges(user: UserID): Future[List[PendingTransaction]] = {
    repo.findChargesByUser(user)
  }

  override def listPayouts(user: UserID): Future[List[PendingTransaction]] = {
    repo.findPayoutsByUser(user)
  }

  override def findUsersWithPayouts(): Future[List[UserID]] = {
    repo.findUsersWithPayouts()
  }

  override def findUsersWithoutCharges(): Future[List[UserID]] = {
    repo.findUsersWithoutCharges()
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

  override def removeCharges(giver: UserID, transactions: Seq[PendingTransaction]): Future[Boolean] = {
    repo.removeCharges(giver, transactions)
  }

  override def removePayouts(user: UserID, transactions: Seq[PendingTransaction]): Future[Boolean] = {
    repo.removePayouts(user, transactions)
  }
}