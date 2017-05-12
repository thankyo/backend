package com.clemble.loveit.user.service

import akka.actor.Actor
import com.clemble.loveit.user.model.UserIdentity
import com.mohiva.play.silhouette.api.{Logger, SignUpEvent}

case class SubscriptionOnSignUpManager(userService: UserService, subscriptionManager: SubscriptionManager) extends Actor with Logger {

  override def receive: Receive = {
    case SignUpEvent(UserIdentity(id, _, _, _, _), _) =>
      userService.findById(id).map(_ match {
        case Some(user) =>
          subscriptionManager.signedUp(user)
        case None =>
      })(context.dispatcher)
  }

}
