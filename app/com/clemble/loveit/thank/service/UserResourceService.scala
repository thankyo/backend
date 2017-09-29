package com.clemble.loveit.thank.service

import javax.inject.{Inject, Singleton}

import akka.actor.{Actor, ActorSystem, Props}
import com.clemble.loveit.common.model.UserID
import com.clemble.loveit.common.util.AuthEnv
import com.clemble.loveit.thank.model.UserResource
import com.clemble.loveit.thank.service.repository.UserResourceRepository
import com.clemble.loveit.user.model.User
import com.mohiva.play.silhouette.api.{Environment, SignUpEvent}

import scala.concurrent.Future

trait UserResourceService {

  def find(user: UserID): Future[Option[UserResource]]

  def createAndSave(user: User): Future[Boolean]

}

case class UserResourceSignUpListener @Inject()(uPayS: UserResourceService) extends Actor {

  override def receive: Receive = {
    case SignUpEvent(user : User, _) =>
      uPayS.createAndSave(user)
  }

}

@Singleton
class SimpleUserResourceService @Inject()(actorSystem: ActorSystem, env: Environment[AuthEnv], repo: UserResourceRepository) extends UserResourceService {

  {
    val subscriber = actorSystem.actorOf(Props(UserResourceSignUpListener(this)))
    env.eventBus.subscribe(subscriber, classOf[SignUpEvent[User]])
  }

  override def createAndSave(user: User) = {
    val uRes = UserResource from user
    repo.save(uRes)
  }

  override def find(user: UserID) = {
    repo.find(user)
  }

}
