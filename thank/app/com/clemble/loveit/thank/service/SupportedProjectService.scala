package com.clemble.loveit.thank.service

import javax.inject.{Inject, Singleton}

import akka.actor.{Actor, ActorSystem, Props}
import com.clemble.loveit.common.model.{Resource, Tag, ThankEvent, UserID}
import com.clemble.loveit.thank.model.SupportedProject
import com.clemble.loveit.thank.service.repository.{SupportTrackRepository, SupportedProjectRepository}

import scala.concurrent.{ExecutionContext, Future}

trait SupportedProjectService {

  def findProject(res: Resource): Future[Option[SupportedProject]]

  def findProjectsByUser(user: UserID): Future[List[SupportedProject]]

  def create(project: SupportedProject): Future[Boolean]

  def assignTags(resource: Resource, tags: Set[Tag]): Future[Boolean]

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

  override def findProject(res: Resource): Future[Option[SupportedProject]] = {
    repo.findProject(res)
  }

  override def create(project: SupportedProject): Future[Boolean] = {
    repo.saveProject(project)
  }

  override def findProjectsByUser(userID: UserID): Future[List[SupportedProject]] = {
    repo.findProjectsByUser(userID)
  }

  override def assignTags(resource: Resource, tags: Set[Tag]): Future[Boolean] = {
    repo.assignTags(resource, tags)
  }

  override def getSupported(user: UserID): Future[List[SupportedProject]] = {
    supTrackRepo.getSupported(user)
  }

  override def markSupported(giver: UserID, project: SupportedProject): Future[Boolean] = {
    supTrackRepo.isSupportedBy(giver, project)
  }

}
