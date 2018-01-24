package com.clemble.loveit.thank.service

import javax.inject.{Inject, Singleton}

import akka.actor.{Actor, Props}
import com.clemble.loveit.common.model.{Resource, UserID}
import com.clemble.loveit.common.util.EventBusManager
import com.clemble.loveit.thank.model.{SupportedProject, UserResource}
import com.clemble.loveit.thank.service.repository.UserResourceRepository
import com.clemble.loveit.user.model.User
import com.mohiva.play.silhouette.api.SignUpEvent

import scala.concurrent.Future

trait UserResourceService {

  def find(user: UserID): Future[Option[UserResource]]

  def findOwner(res: Resource): Future[Option[SupportedProject]]

  def create(user: User): Future[Boolean]

}

case class UserResourceSignUpListener @Inject()(uPayS: UserResourceService) extends Actor {

  override def receive: Receive = {
    case SignUpEvent(user: User, _) =>
      uPayS.create(user)
  }

}

@Singleton
class SimpleUserResourceService @Inject()(
                                           repo: UserResourceRepository,
                                           eventBusManager: EventBusManager
                                         ) extends UserResourceService {

  eventBusManager.onSignUp(Props(UserResourceSignUpListener(this)))

  override def create(user: User) = {
    repo.save(UserResource from user)
  }

  override def findOwner(res: Resource): Future[Option[SupportedProject]] = {
    repo.findOwner(res)
  }

  override def find(user: UserID) = {
    repo.find(user)
  }

}
