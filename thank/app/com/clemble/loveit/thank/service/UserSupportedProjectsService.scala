package com.clemble.loveit.thank.service

import javax.inject.{Inject, Singleton}

import akka.actor.{Actor, ActorSystem, Props}
import com.clemble.loveit.common.model.{ThankTransaction, UserID}
import com.clemble.loveit.thank.service.repository.UserSupportedProjectsRepo
import com.clemble.loveit.user.model.User
import com.clemble.loveit.user.service.UserService

import scala.concurrent.{ExecutionContext, Future}

trait UserSupportedProjectsService {

  def getSupported(user: UserID): Future[List[User]]

  def markSupported(supporter: UserID, project: UserID): Future[Boolean]

}

case class SupportedProjectsThankListener(service: UserSupportedProjectsService) extends Actor {
  override def receive = {
    case ThankTransaction(giver, owner, _, _) =>
      service.markSupported(giver, owner)
  }
}

@Singleton
class SimpleUserSupportedProjectsService @Inject()(
                                                    actorSystem: ActorSystem,
                                                    thankEventBus: ThankEventBus,
                                                    userService: UserService,
                                                    repo: UserSupportedProjectsRepo,
                                                    implicit val ec: ExecutionContext
                                                  ) extends UserSupportedProjectsService {

  {
    val subscriber = actorSystem.actorOf(Props(SupportedProjectsThankListener(this)))
    thankEventBus.subscribe(subscriber, classOf[ThankTransaction])
  }

  override def getSupported(user: UserID): Future[List[User]] = {
    repo.getSupported(user)
  }

  override def markSupported(supporter: UserID, project: UserID): Future[Boolean] = {
    for {
      projectOpt <- userService.findById(project) if (projectOpt.isDefined)
      updated <- repo.markSupported(supporter, projectOpt.get)
    } yield {
      updated
    }
  }

}
