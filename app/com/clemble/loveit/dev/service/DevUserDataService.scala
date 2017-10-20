package com.clemble.loveit.dev.service

import javax.inject.Inject

import akka.actor.{Actor, ActorSystem, Props}
import com.clemble.loveit.common.model.{HttpResource, Resource, UserID}
import com.clemble.loveit.common.util.{AuthEnv, IDGenerator}
import com.clemble.loveit.thank.service.{ROService, ThankService}
import com.clemble.loveit.user.model.User
import com.clemble.loveit.user.service.UserService
import com.mohiva.play.silhouette.api._

import scala.concurrent.{ExecutionContext, Future, Promise}
import scala.concurrent.duration._

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
    case SignUpEvent(user: User, _) =>
      runThanks(user.id)
    case LoginEvent(user: User, _) =>
      runThanks(user.id)
  }
}

trait DevUserDataService {

  def enable(resMap: Map[User, Resource]): Future[Boolean]

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
    User(
      id = IDGenerator.generate(),
      firstName = Some("Gavin"),
      lastName = Some("Than"),
      email = "gavin.than@example.com",
      profiles = Set(LoginInfo("patreon", "zenpencil")),
      avatar = Some("https://pbs.twimg.com/profile_images/493961823763181568/mb_2vK6y_400x400.jpeg"),
      link = Some("https://zenpencils.com")
    ) -> HttpResource("zenpencils.com")
  )

  enable(resMap)


  override def enable(resMap: Map[User, Resource]): Future[Boolean] = {
    (for {
      creatorsToRes <- ensureCreators(resMap)
      resources <- assignOwnership(creatorsToRes)
    } yield {
      val subscriber = actorSystem.actorOf(Props(DevSignUpEventListener(resources, thankService)))
      env.eventBus.subscribe(subscriber, classOf[SignUpEvent[User]])
      env.eventBus.subscribe(subscriber, classOf[LoginEvent[User]])
    }).recover({
      case t: Throwable => {
        print(t)
        System.exit(1)
        false
      }
    })
  }

  private def ensureCreators(resMap: Map[User, Resource]): Future[Map[User, Resource]] = {
    val fCreators = for {
      (creator, resources) <- resMap
    } yield {
      userService.
        findByEmail(creator.email).
        flatMap({
          case Some(user) => Future.successful(user -> resources)
          case None => {
            userService.save(creator).map(creator => {
              env.eventBus.publish(SignUpEvent(creator, null))
              creator -> resources
            })
          }
        })
    }
    Future.sequence(fCreators).map(_.toMap)
  }

  private def assignOwnership(creatorToRes: Map[User, Resource]): Future[List[Resource]] = {
    val resources = for {
      (creator, resource) <- creatorToRes
    } yield {
      val ownershipTask = roService.
        assignOwnership(creator.id, resource).
        recoverWith({
          case _: Throwable =>
            Thread.sleep(10000)
            roService.assignOwnership(creator.id, resource)
          }
        )
      ownershipTask.
        map(res => (1 to 200).
        map(i => HttpResource(s"${res.uri}/comics/${i}")))
    }
    Future.sequence(resources).map(_.flatten.toList)
  }

}
