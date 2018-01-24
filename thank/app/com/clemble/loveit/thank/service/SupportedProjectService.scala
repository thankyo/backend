package com.clemble.loveit.thank.service

import javax.inject.{Inject, Singleton}

import akka.actor.{Actor, ActorSystem, Props}
import com.clemble.loveit.common.model.{ThankEvent, UserID}
import com.clemble.loveit.thank.model.SupportedProject
import com.clemble.loveit.thank.service.repository.{SupportTrackRepository, SupportedProjectRepository}
import com.clemble.loveit.user.service.UserService

import scala.concurrent.{ExecutionContext, Future}

trait SupportedProjectService {

  def getProject(project: UserID): Future[Option[SupportedProject]]

  def getSupported(user: UserID): Future[List[SupportedProject]]

  def markSupported(supporter: UserID, project: SupportedProject): Future[Boolean]

}

case class SupportedProjectsThankListener(service: SupportedProjectService) extends Actor {
  override def receive = {
    case ThankEvent(giver, owner, _, _) =>
      service.markSupported(giver, owner)
  }
}

@Singleton
class SimpleSupportedProjectService @Inject()(
                                               actorSystem: ActorSystem,
                                               thankEventBus: ThankEventBus,
                                               repo: SupportedProjectRepository,
                                               supTrackRepo: SupportTrackRepository,
                                               implicit val ec: ExecutionContext
                                                  ) extends SupportedProjectService {

  {
    val subscriber = actorSystem.actorOf(Props(SupportedProjectsThankListener(this)))
    thankEventBus.subscribe(subscriber, classOf[ThankEvent])
  }


  override def getProject(userID: UserID) = {
    repo.getProject(userID)
  }

  override def getSupported(user: UserID): Future[List[SupportedProject]] = {
    supTrackRepo.getSupported(user)
  }

  override def markSupported(giver: UserID, project: SupportedProject): Future[Boolean] = {
    supTrackRepo.markSupported(giver, project)
  }

}
