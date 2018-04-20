package com.clemble.loveit.thank.service

import akka.actor.{Actor, Props}
import com.clemble.loveit.common.model.User
import com.clemble.loveit.common.util.EventBusManager
import com.clemble.loveit.thank.model.UserProjects
import com.clemble.loveit.thank.service.repository.UserProjectsRepository
import com.mohiva.play.silhouette.api.SignUpEvent
import javax.inject.{Inject, Singleton}

import scala.concurrent.Future

trait UserProjectsService {

  def create(user: User): Future[UserProjects]

}

case class UserProjectsServiceSignUpListener @Inject()(uPrjS: UserProjectsService) extends Actor {

  override def receive: Receive = {
    case SignUpEvent(user: User, _) =>
      uPrjS.create(user)
  }

}

@Singleton
class SimpleUserProjectsService @Inject()(
  eventBusManager: EventBusManager,
  repo: UserProjectsRepository
) extends UserProjectsService {

  eventBusManager.onSignUp(Props(UserProjectsServiceSignUpListener(this)))

  override def create(user: User): Future[UserProjects] = {
    val projects = UserProjects(user.id, Seq.empty, Seq.empty)
    repo.save(projects)
  }


}
