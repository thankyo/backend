package com.clemble.loveit.thank.service

import com.clemble.loveit.common.model.{Resource, UserID}
import com.clemble.loveit.payment.service.ThankTransactionService
import com.clemble.loveit.thank.model.Thank
import com.clemble.loveit.thank.service.repository.ThankRepository
import javax.inject.{Inject, Singleton}

import com.clemble.loveit.common.error.PaymentException

import scala.concurrent.{ExecutionContext, Future}

trait ThankService {

  def getOrCreate(uri: Resource): Future[Thank]

  def thank(giver: UserID, uri: Resource): Future[Thank]

}

@Singleton
case class SimpleThankService @Inject()(
                                         transactionService: ThankTransactionService,
                                         thankRepo: ThankRepository,
                                         implicit val ec: ExecutionContext
) extends ThankService {

  override def getOrCreate(resource: Resource): Future[Thank] = {
    def createIfMissing(thankOpt: Option[Thank]): Future[Thank] = {
      thankOpt match {
        case Some(thank) => Future.successful(thank)
        case None =>
          resource.parent() match {
            case Some(parRes) =>
              for {
                owner <- getOrCreate(parRes).map(_.owner)
                thank = Thank(resource, owner)
                createdNew <- thankRepo.save(thank)
                created <- if(createdNew) Future.successful(thank) else thankRepo.findByResource(resource).map(_.get)
              } yield {
                created
              }
            case None => // TODO define proper error handling here
              throw new IllegalArgumentException()
          }
      }
    }

    thankRepo.findByResource(resource).flatMap(createIfMissing)
  }

  override def thank(giver: UserID, res: Resource): Future[Thank] = {
    for {
      thank <- getOrCreate(res) // Ensure Thank exists
      increase <- thankRepo.increase(giver, res)
    } yield {
      if (!increase) throw PaymentException.alreadyThanked(giver, res)
      transactionService.create(giver, thank.owner, res) // TODO need to handle failure properly
      thank
    }
  }

}