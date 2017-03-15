package com.clemble.thank.service.impl

import com.clemble.thank.model.{Thank, Resource, UserID}
import com.clemble.thank.service.repository.{ThankRepository}
import com.clemble.thank.service.{ThankService, UserPaymentService}
import com.google.inject.{Inject, Singleton}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
case class SimpleThankService @Inject()(paymentService: UserPaymentService, repository: ThankRepository, implicit val ec: ExecutionContext) extends ThankService {

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
      _ <- paymentService.operation(user, resource, 1)
      _ <- repository.increase(resource)
      updated <- repository.findByResource(resource).map(_.get)
    } yield {
      updated
    }
  }

}
