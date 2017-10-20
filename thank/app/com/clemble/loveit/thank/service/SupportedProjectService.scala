package com.clemble.loveit.thank.service

import javax.inject.{Inject, Singleton}

import akka.actor.{Actor, ActorSystem, Props}
import com.clemble.loveit.common.model.{ThankEvent, UserID}
import com.clemble.loveit.thank.model.SupportedProject
import com.clemble.loveit.thank.service.repository.SupportedProjectRepo
import com.clemble.loveit.user.service.UserService

import scala.concurrent.{ExecutionContext, Future}

trait SupportedProjectService {

  def getSupported(user: UserID): Future[List[SupportedProject]]

  def markSupported(supporter: UserID, project: UserID): Future[Boolean]

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
                                                userService: UserService,
                                                repo: SupportedProjectRepo,
                                                implicit val ec: ExecutionContext
                                                  ) extends SupportedProjectService {

  {
    val subscriber = actorSystem.actorOf(Props(SupportedProjectsThankListener(this)))
    thankEventBus.subscribe(subscriber, classOf[ThankEvent])
  }

  override def getSupported(user: UserID): Future[List[SupportedProject]] = {
    repo.getSupported(user)
  }

  override def markSupported(supporterId: UserID, ownerId: UserID): Future[Boolean] = {
    for {
      ownerOpt <- userService.findById(ownerId)
      updated <- repo.markSupported(supporterId, SupportedProject from ownerOpt.get)
    } yield {
      updated
    }
  }

}
