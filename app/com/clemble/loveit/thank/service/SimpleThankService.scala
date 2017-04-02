package com.clemble.loveit.thank.service

import com.clemble.loveit.common.model.{Resource, UserID}
import com.clemble.loveit.payment.service.ThankTransactionService
import com.clemble.loveit.thank.model.Thank
import com.clemble.loveit.thank.service.repository.ThankRepository
import com.google.inject.{Inject, Singleton}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
case class SimpleThankService @Inject()(paymentService: ThankTransactionService, repository: ThankRepository, implicit val ec: ExecutionContext) extends ThankService {

  override def getOrCreate(resource: Resource): Future[Thank] = {
    def createIfMissing(thankOpt: Option[Thank]): Future[Thank] = {
      thankOpt match {
        case Some(thank) => Future.successful(thank)
        case None => repository.
          save(Thank(resource)).
          flatMap(_ => repository.findByResource(resource).map(_.get))
      }
    }

    repository.findByResource(resource).flatMap(createIfMissing)
  }

  override def thank(user: UserID, resource: Resource): Future[Thank] = {
    for {
      _ <- getOrCreate(resource) // Ensure Thank exists
      _ <- paymentService.create(user, resource, 1)
      _ <- repository.increase(resource)
      updated <- repository.findByResource(resource).map(_.get)
    } yield {
      updated
    }
  }

}
