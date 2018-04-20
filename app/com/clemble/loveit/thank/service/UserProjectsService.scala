package com.clemble.loveit.thank.service

import akka.actor.{Actor, Props}
import com.clemble.loveit.common.model.{User, UserID}
import com.clemble.loveit.common.util.EventBusManager
import com.clemble.loveit.thank.model.UserProjects
import com.clemble.loveit.thank.service.repository.UserProjectsRepository
import com.mohiva.play.silhouette.api.{LoginEvent, SignUpEvent}
import javax.inject.{Inject, Singleton}

import scala.concurrent.{ExecutionContext, Future}

trait UserProjectsService {

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

  override def updateOwned(user: UserID): Future[UserProjects] = {
    ownershipService
      .fetch(user)
      .flatMap(owned => {
        repo.saveOwnedProject(user, owned)
      })
  }

}
