package com.clemble.loveit.thank.service

import akka.actor.{Actor, ActorRef, Props}
import com.clemble.loveit.common.model.{HttpResource, UserID}
import com.clemble.loveit.thank.model._

sealed trait ThankCommands
case class GetCmd(path: List[String])
case class ThankCmd(path: List[String])
case class AssignOwnershipCmd(path: List[String], user: UserID, ownership: ResourceOwnership)

class HttpLoveItActor(resource: HttpResource) extends Actor{

  private var thank = Thank(
    resource
  )

  private def getOrCreate(path: String): ActorRef = {
    context.child(path).getOrElse({
      context.actorOf(Props(new HttpLoveItActor(resource.append(path))))
    })
  }

  override def receive: Receive = {
    case GetCmd(Nil) => sender ! thank
    case GetCmd(path :: tail) =>
      val child = getOrCreate(path)
      (child ! GetCmd(tail))(sender())
    case ThankCmd(Nil) =>
      thank = thank.inc()
      sender() ! thank
    case ThankCmd(path :: tail) =>
      thank = thank.inc()
      val child = getOrCreate(path)
      (child ! ThankCmd(tail))(sender())
    case AssignOwnershipCmd(Nil, user, FullResourceOwnership(_)) =>

    case AssignOwnershipCmd(Nil, user, PartialResourceOwnership(_)) =>
      thank = thank.setOwner(user)

    case AssignOwnershipCmd(Nil, user, UnrealizedResourceOwnership(_)) =>
      thank = thank.setOwner(user)

    case AssignOwnershipCmd(path :: tail, user, ownership) =>
      val child = getOrCreate(path)
      (child ! AssignOwnershipCmd(tail, user, ownership))(sender())
  }

}
