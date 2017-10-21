package com.clemble.loveit.common.util

import akka.actor.{ActorSystem, Props}
import com.mohiva.play.silhouette.api.{Environment, LoginEvent, SignUpEvent, SilhouetteEvent}

case class EventBusManager(env: Environment[AuthEnv], actorSystem: ActorSystem) {

  def publish(event: SilhouetteEvent): Unit = {
    env.eventBus.publish(event)
  }

  def onSignUp(props: Props) = {
    val subscriber = actorSystem.actorOf(props)
    env.eventBus.subscribe(subscriber, classOf[SignUpEvent[AuthEnv]])
  }

  def onLogin(props: Props) = {
    val subscriber = actorSystem.actorOf(props)
    env.eventBus.subscribe(subscriber, classOf[LoginEvent[AuthEnv]])
  }

}
