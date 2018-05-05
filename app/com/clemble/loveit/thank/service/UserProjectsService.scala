package com.clemble.loveit.thank.service

import akka.actor.{Actor, Props}
import com.clemble.loveit.common.model.{Resource, User, UserID}
import com.clemble.loveit.common.service.URLValidator
import com.clemble.loveit.common.util.EventBusManager
import com.clemble.loveit.thank.model.UserProject
import com.clemble.loveit.thank.service.repository.UserProjectsRepository
import com.mohiva.play.silhouette.api.{LoginEvent, SignUpEvent}
import javax.inject.{Inject, Singleton}

import scala.concurrent.{ExecutionContext, Future}

trait UserProjectsService {

  def get(user: UserID): Future[UserProject]

  def create(user: User): Future[UserProject]

}

case class UserProjectsServiceSignUpListener @Inject()(uPrjS: UserProjectsService) extends Actor {

  override def receive: Receive = {
    case SignUpEvent(user: User, _) =>
      uPrjS.create(user)
  }

}

@Singleton
case class SimpleUserProjectsService @Inject()(
  eventBusManager: EventBusManager,
  emailVerSvc: EmailProjectOwnershipService,
  urlValidator: URLValidator,
  repo: UserProjectsRepository,
  implicit val ec: ExecutionContext
) extends UserProjectsService {

  eventBusManager.onSignUp(Props(UserProjectsServiceSignUpListener(this)))

  override def create(user: User): Future[UserProject] = {
    val projects = UserProject(user.id, Seq.empty, Seq.empty)
    repo.save(projects)
  }

  override def get(user: UserID): Future[UserProject] = {
    repo.findById(user).map(_.get)
  }

}
