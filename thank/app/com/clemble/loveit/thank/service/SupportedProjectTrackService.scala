package com.clemble.loveit.thank.service

import javax.inject.{Inject, Singleton}

import akka.actor.{Actor, ActorSystem, Props}
import com.clemble.loveit.common.model.{ThankEvent, UserID}
import com.clemble.loveit.thank.model.SupportedProject
import com.clemble.loveit.thank.service.repository.{SupportTrackRepository, SupportedProjectRepository}

import scala.concurrent.{ExecutionContext, Future}

trait SupportedProjectTrackService {

  def getSupported(user: UserID): Future[List[SupportedProject]]

  def markSupported(supporter: UserID, project: SupportedProject): Future[Boolean]

}

case class SupportedProjectsThankListener(service: SupportedProjectTrackService) extends Actor {
  override def receive = {
    case ThankEvent(giver, owner, _, _) =>
      service.markSupported(giver, owner)
  }
}


@Singleton
class SimpleSupportedProjectTrackService @Inject()(
                                                    actorSystem: ActorSystem,
                                                    thankEventBus: ThankEventBus,
                                                    repo: SupportedProjectRepository,
                                                    supTrackRepo: SupportTrackRepository,
                                                    implicit val ec: ExecutionContext
                                                  ) extends SupportedProjectTrackService {

  {
    val subscriber = actorSystem.actorOf(Props(SupportedProjectsThankListener(this)))
    thankEventBus.subscribe(subscriber, classOf[ThankEvent])
  }

  override def getSupported(user: UserID): Future[List[SupportedProject]] = {
    supTrackRepo.getSupported(user).flatMap(repo.findAll)
  }

  override def markSupported(supporter: UserID, project: SupportedProject): Future[Boolean] = {
    supTrackRepo.markSupportedBy(supporter, project)
  }

}