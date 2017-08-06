package com.clemble.loveit.user.service

import akka.actor.{Actor, ActorSystem, Props}
import akka.event.EventBus
import com.clemble.loveit.common.model.{Resource, UserID}
import com.clemble.loveit.thank.service.{ROService, ThankService}
import com.clemble.loveit.user.model.UserIdentity
import com.mohiva.play.silhouette.api.SignUpEvent
import com.mohiva.play.silhouette.impl.providers.CommonSocialProfile

import scala.concurrent.{ExecutionContext, Future}

case class TestSignUpEventListener(resources: List[Resource], thankService: ThankService) extends Actor {
  override def receive: Receive = {
    case SignUpEvent(giver: UserIdentity, _) => {
      for {
        res <- resources
      } yield {
        thankService.thank(giver.id, res)
      }
    }
  }
}

/**
  * Service that creates first users and integrations for testing UI and UX
  */
case class TestUserDataService(userService: UserService, roService: ROService, thankService: ThankService, implicit val ec: ExecutionContext) {

  def enable(resMap: Map[CommonSocialProfile, Resource], eventBus: EventBus, actorSystem: ActorSystem) = {
    for {
      creatorsToRes <- ensureCreators(resMap)
      resources <- assignOwnership(creatorsToRes)
    } yield {
      val subscriber = actorSystem.actorOf(Props(TestSignUpEventListener(resources, thankService)))
      actorSystem.eventStream.subscribe(subscriber, classOf[SignUpEvent[UserIdentity]])
    }
  }

  private def ensureCreators(resMap: Map[CommonSocialProfile, Resource]): Future[Map[UserIdentity, Resource]] = {
    val fCreators = for {
      creator <- resMap.keys
    } yield {
      userService.
        createOrUpdateUser(creator).
        map(_ match {
          case Left(user) => user
          case Right(user) => user
        }).
        map(user => user -> resMap(creator))
    }
    Future.sequence(fCreators).map(_.toMap)
  }

  private def assignOwnership(creatorToRes: Map[UserIdentity, Resource]): Future[List[Resource]] = {
    val resources = for {
      (creator, resource) <- creatorToRes
    } yield {
      roService.assignOwnership(creator.id, resource)
    }
    Future.sequence(resources).map(_.toList)
  }

}
