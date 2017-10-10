package com.clemble.loveit.payment.service

import javax.inject.{Inject, Singleton}

import akka.actor.{Actor, ActorSystem, Props}
import akka.stream.scaladsl.Source
import com.clemble.loveit.common.model.{Resource, ThankTransaction, UserID}
import com.clemble.loveit.payment.service.repository.{ThankTransactionRepository, UserBalanceRepository}
import com.clemble.loveit.thank.service.{ThankEventBus}
import com.mohiva.play.silhouette.api.Logger

import scala.concurrent.{ExecutionContext, Future}

trait ThankTransactionService {

  def list(user: UserID): Source[ThankTransaction, _]

  def create(giver: UserID, owner: UserID, url: Resource): Future[ThankTransaction]

  def removeAll(thank: Seq[ThankTransaction]): Future[Boolean]
}

case class PaymentThankListener(service: ThankTransactionService) extends Actor {
  override def receive = {
    case ThankTransaction(giver, owner, res, _) =>
      service.create(giver, owner, res)
  }
}

@Singleton
case class SimpleThankTransactionService @Inject()(
                                                    actorSystem: ActorSystem,
                                                    thankEventBus: ThankEventBus,
                                                    balanceRepo: UserBalanceRepository,
                                                    repository: ThankTransactionRepository,
                                                    implicit val ec: ExecutionContext
                                                  ) extends ThankTransactionService with Logger {

  {
    val subscriber = actorSystem.actorOf(Props(PaymentThankListener(this)))
    thankEventBus.subscribe(subscriber, classOf[ThankTransaction])
  }

  override def list(user: UserID): Source[ThankTransaction, _] = {
    repository.findByUser(user)
  }

  override def create(giver: UserID, owner: UserID, url: Resource): Future[ThankTransaction] = {
    val transaction = ThankTransaction(giver, owner, url)
    for {
      savedInRepo <- repository.save(transaction)
      updatedOwner <- balanceRepo.updateBalance(owner, 1) if (savedInRepo)
      updatedGiver <- balanceRepo.updateBalance(giver, -1) if (savedInRepo)
    } yield {
      if (!updatedGiver || !updatedOwner || !savedInRepo)
        logger.error(s"${giver} ${owner} ${url} failed to properly process transaction ${updatedGiver} ${updatedOwner} ${savedInRepo}")
      transaction
    }
  }

  override def removeAll(thanks: Seq[ThankTransaction]): Future[Boolean] = {
    repository.removeAll(thanks)
  }

}