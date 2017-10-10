package com.clemble.loveit.thank.service

import com.clemble.loveit.common.model.{Resource, ThankTransaction, UserID}
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
                                         thankEventBus: ThankEventBus,
                                         thankRepo: ThankRepository,
                                         userStatRepo: UserStatRepo,
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
    for {
      thank <- getOrCreate(res) // Ensure Thank exists
      increased <- thankRepo.increase(giver, res)
    } yield {
      if (increased) {
        thankEventBus.publish(ThankTransaction(giver, thank.owner, res))
        userStatRepo.record(thank)
      }
      thank
    }
  }

}