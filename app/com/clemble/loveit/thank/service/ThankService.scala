package com.clemble.loveit.thank.service

import com.clemble.loveit.common.model.{Resource, UserID}
import com.clemble.loveit.payment.service.ThankTransactionService
import com.clemble.loveit.thank.model.Thank
import com.clemble.loveit.thank.service.repository.ThankRepository
import javax.inject.{Inject, Singleton}

import scala.concurrent.{ExecutionContext, Future}

trait ThankService {

  def getOrCreate(uri: Resource): Future[Thank]

  def thank(user: UserID, uri: Resource): Future[Thank]

}

@Singleton
case class SimpleThankService @Inject()(
                                         transactionService: ThankTransactionService,
                                         repository: ThankRepository,
                                         implicit val ec: ExecutionContext
) extends ThankService {

  override def getOrCreate(resource: Resource): Future[Thank] = {
    def createIfMissing(thankOpt: Option[Thank]): Future[Thank] = {
      resource.parent() match {
        case Some(parent) =>
          for {
            parentThank <- repository.findByResource(parent).flatMap(createIfMissing)
            _ <- repository.save(Thank(resource, parentThank.owner))
            retrieved <- repository.findByResource(resource).map(_.get)
          } yield {
            retrieved
          }
        case None => // TODO define proper error handling here
          throw new IllegalArgumentException()
      }
    }

    repository.findByResource(resource).flatMap(createIfMissing)
  }

  override def thank(user: UserID, resource: Resource): Future[Thank] = {
    for {
      _ <- getOrCreate(resource) // Ensure Thank exists
      increased <- repository.increase(user, resource) if (increased)
      _ <- transactionService.create(user, resource)
      updated <- repository.findByResource(resource).map(_.get)
    } yield {
      updated
    }
  }

}