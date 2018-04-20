package com.clemble.loveit.thank.service

import akka.actor.{Actor, Props}
import com.clemble.loveit.common.model.{OwnedProject, Resource, User, UserID}
import com.clemble.loveit.common.util.EventBusManager
import com.clemble.loveit.thank.model.UserProjects
import com.clemble.loveit.thank.service.repository.UserProjectsRepository
import com.mohiva.play.silhouette.api.{LoginEvent, SignUpEvent}
import javax.inject.{Inject, Singleton}

import scala.concurrent.{ExecutionContext, Future}

trait UserProjectsService {

  def get(user: UserID): Future[UserProjects]

  def dibsOnUrl(user: UserID, url: Resource): Future[OwnedProject]

  def create(user: User): Future[UserProjects]

  def updateOwned(user: UserID): Future[UserProjects]

}

case class UserProjectsServiceSignUpListener @Inject()(uPrjS: UserProjectsService) extends Actor {

  override def receive: Receive = {
    case SignUpEvent(user: User, _) =>
      uPrjS.create(user)
    case LoginEvent(user: User, _) =>
      // TODO this will trigger ownership revalidation on every log in, which is very bad need to optimize this
      uPrjS.updateOwned(user.id)
  }

}

@Singleton
class SimpleUserProjectsService @Inject()(
  eventBusManager: EventBusManager,
  projectEnrichService: ProjectEnrichService,
  ownershipService: ProjectOwnershipService,
  repo: UserProjectsRepository,
  implicit val ec: ExecutionContext
) extends UserProjectsService {

  eventBusManager.onSignUp(Props(UserProjectsServiceSignUpListener(this)))
  eventBusManager.onLogin(Props(UserProjectsServiceSignUpListener(this)))

  override def create(user: User): Future[UserProjects] = {
    val projects = UserProjects(user.id, Seq.empty, Seq.empty)
    repo.save(projects)
  }

  override def dibsOnUrl(user: UserID, url: Resource): Future[OwnedProject] = {
    for {
      ownedProject <- projectEnrichService.enrich(user, url)
      _ <- repo.saveOwnedProject(user, Seq(ownedProject))
    } yield {
      ownedProject
    }
  }

  override def get(user: UserID): Future[UserProjects] = {
    repo.findById(user).map(_.get)
  }

  override def updateOwned(user: UserID): Future[UserProjects] = {
    ownershipService
      .fetch(user)
      .flatMap(owned => {
        repo.saveOwnedProject(user, owned)
      })
  }

}
