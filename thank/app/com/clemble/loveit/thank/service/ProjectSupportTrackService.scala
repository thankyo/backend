package com.clemble.loveit.thank.service

import javax.inject.{Inject, Singleton}

import akka.actor.{Actor, ActorSystem, Props}
import com.clemble.loveit.common.model.{ThankEvent, UserID}
import com.clemble.loveit.thank.model.Project
import com.clemble.loveit.thank.service.repository.{ProjectSupportTrackRepository, ProjectRepository}

import scala.concurrent.{ExecutionContext, Future}

trait ProjectSupportTrackService {

  def getSupported(user: UserID): Future[List[Project]]

  def markSupported(supporter: UserID, project: Project): Future[Boolean]

}

case class ProjectSupportThankListener(service: ProjectSupportTrackService) extends Actor {
  override def receive = {
    case ThankEvent(giver, owner, _, _) =>
      service.markSupported(giver, owner)
  }
}


@Singleton
class SimpleProjectSupportTrackService @Inject()(
                                                  actorSystem: ActorSystem,
                                                  thankEventBus: ThankEventBus,
                                                  repo: ProjectRepository,
                                                  supTrackRepo: ProjectSupportTrackRepository,
                                                  implicit val ec: ExecutionContext
                                                  ) extends ProjectSupportTrackService {

  {
    val subscriber = actorSystem.actorOf(Props(ProjectSupportThankListener(this)))
    thankEventBus.subscribe(subscriber, classOf[ThankEvent])
  }

  override def getSupported(user: UserID): Future[List[Project]] = {
    supTrackRepo.getSupported(user).flatMap(repo.findAll)
  }

  override def markSupported(supporter: UserID, project: Project): Future[Boolean] = {
    supTrackRepo.markSupportedBy(supporter, project)
  }

}