package com.clemble.loveit.dev.service

import javax.inject.Inject

import akka.actor.{Actor, ActorSystem, Props}
import com.clemble.loveit.common.model.{HttpResource, Resource, UserID}
import com.clemble.loveit.common.util.AuthEnv
import com.clemble.loveit.thank.service.{ROService, ThankService}
import com.clemble.loveit.user.model.UserIdentity
import com.clemble.loveit.user.service.UserService
import com.mohiva.play.silhouette.api._
import com.mohiva.play.silhouette.impl.providers.CommonSocialProfile

import scala.concurrent.{ExecutionContext, Future}

case class DevSignUpEventListener(resources: List[Resource], thankService: ThankService) extends Actor {

  implicit val ec = context.dispatcher

  def runThanks(giver: UserID) = {
    for {
      res <- resources
    } yield {
      thankService.thank(giver, res)
    }
  }

  override def receive: Receive = {
    case SignUpEvent(UserIdentity(id, _, _, _, _), _) =>
      runThanks(id)
    case LoginEvent(UserIdentity(id, _, _, _, _), _) =>
      runThanks(id)
  }
}

trait DevUserDataService {

  def enable(resMap: Map[CommonSocialProfile, Resource]): Future[Boolean]

}

/**
  * Service that creates first users and integrations for testing UI and UX
  */
case class SimpleDevUserDataService @Inject()(
                                               userService: UserService,
                                               roService: ROService,
                                               thankService: ThankService,
                                               actorSystem: ActorSystem,
                                               env: Environment[AuthEnv],
                                               implicit val ec: ExecutionContext
) extends DevUserDataService {

  val resMap = Map(
    CommonSocialProfile(
      loginInfo = LoginInfo("patreon", "zenpencil"),
      firstName = Some("Gavin"),
      lastName = Some("Than"),
      fullName = Some("Gavin Aung Than"),
      email = None,
      avatarURL = Some("https://pbs.twimg.com/profile_images/493961823763181568/mb_2vK6y_400x400.jpeg")
    ) -> HttpResource("zenpencils.com")
  )

  enable(resMap)


  override def enable(resMap: Map[CommonSocialProfile, Resource]): Future[Boolean] = {
    for {
      creatorsToRes <- ensureCreators(resMap)
      resources <- assignOwnership(creatorsToRes)
    } yield {
      val subscriber = actorSystem.actorOf(Props(DevSignUpEventListener(resources, thankService)))
      env.eventBus.subscribe(subscriber, classOf[SignUpEvent[UserIdentity]])
      env.eventBus.subscribe(subscriber, classOf[LoginEvent[UserIdentity]])
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
      roService.assignOwnership(creator.id, resource).
        map(res => (1 to 200).map(i => HttpResource(s"${res.uri}/comics/${i}")))
    }
    Future.sequence(resources).map(_.flatten.toList)
  }

}
