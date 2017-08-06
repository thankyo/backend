package com.clemble.loveit.thank.service

import com.clemble.loveit.common.model.{Resource, UserID}
import com.clemble.loveit.payment.service.ThankTransactionService
import com.clemble.loveit.thank.model.Thank
import com.clemble.loveit.thank.service.repository.{ThankRepository, UserStatRepo}
import javax.inject.{Inject, Singleton}

import com.clemble.loveit.common.error.ResourceException

import scala.concurrent.{ExecutionContext, Future}

trait ThankService {

  def hasThanked(giver: UserID, uri: Resource): Future[Boolean]

  def getOrCreate(uri: Resource): Future[Thank]

  def thank(giver: UserID, uri: Resource): Future[Thank]

}

@Singleton
case class SimpleThankService @Inject()(
                                         transactionService: ThankTransactionService,
                                         thankRepo: ThankRepository,
                                         userStatRepo: UserStatRepo,
                                         supportedProjectsService: UserSupportedProjectsService,
                                         implicit val ec: ExecutionContext
) extends ThankService {

  override def hasThanked(giver: UserID, res: Resource): Future[Boolean] = {
    thankRepo.thanked(giver, res).flatMap(_ match {
      case Some(thanked) => Future.successful(thanked)
      case None => getOrCreate(res).map(thank => thank.thankedBy(giver))
    })
  }

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
              throw ResourceException.ownerMissing()
          }
      }
    }

    thankRepo.findByResource(resource).flatMap(createIfMissing)
  }

  override def thank(giver: UserID, res: Resource): Future[Thank] = {
    val fThank = getOrCreate(res)
    val fTransaction = fThank.
      filter(t => !t.thankedBy(giver)).
      flatMap(t => {
        supportedProjectsService.markSupported(giver, t.owner)
        thankRepo.increase(giver, t.resource).
          filter(_ == true).
          flatMap(_ => transactionService.create(giver, t.owner, t.resource))
      }).recover({ case _ => List.empty})
    for {
      thank <- fThank // Ensure Thank exists
      _ <- fTransaction
    } yield {
      userStatRepo.record(thank)
      thank
    }
  }

}