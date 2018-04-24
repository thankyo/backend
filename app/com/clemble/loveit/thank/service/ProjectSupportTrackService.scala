package com.clemble.loveit.thank.service

import javax.inject.{Inject, Singleton}
import akka.actor.{Actor, ActorSystem, Props}
import com.clemble.loveit.common.model.{Project, ProjectPointer, ThankEvent, UserID}
import com.clemble.loveit.common.service.ThankEventBus
import com.clemble.loveit.thank.service.repository.{ProjectRepository, ProjectSupportTrackRepository}

import scala.concurrent.{ExecutionContext, Future}

trait ProjectSupportTrackService {

  def getSupported(user: UserID): Future[List[Project]]

  def markSupported(supporter: UserID, project: ProjectPointer): Future[Boolean]

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
    supTrackRepo.getSupported(user).flatMap(repo.findAllProjects)
  }

  override def markSupported(supporter: UserID, projectPointer: ProjectPointer): Future[Boolean] = {
    for {
      projectOpt <- repo.findProjectById(projectPointer._id)
      supported <- supTrackRepo.markSupportedBy(supporter, projectOpt.get)
    } yield {
      supported
    }
  }

}