package com.clemble.loveit.user.service

import akka.actor.Actor
import com.clemble.loveit.user.model.{User}
import com.mohiva.play.silhouette.api.{Logger, SignUpEvent}

case class SubscriptionOnSignUpManager(userService: UserService, subscriptionManager: SubscriptionManager) extends Actor with Logger {

  override def receive: Receive = {
    case SignUpEvent(user: User, _) =>
      userService.findById(user.id).map(_ match {
        case Some(user) =>
          subscriptionManager.signedUpUser(user)
        case None =>
      })(context.dispatcher)
  }

}
