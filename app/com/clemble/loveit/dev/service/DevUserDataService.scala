package com.clemble.loveit.dev.service

import javax.inject.Inject

import akka.actor.{Actor, Props}
import com.clemble.loveit.common.model.{Resource, Tag, UserID}
import com.clemble.loveit.common.util.{EventBusManager, IDGenerator}
import com.clemble.loveit.thank.service.{PostService, ROService, SupportedProjectService}
import com.clemble.loveit.user.model.User
import com.clemble.loveit.user.service.UserService
import com.mohiva.play.silhouette.api._

import scala.concurrent.{ExecutionContext, Future}

case class DevSignUpListener(resources: Seq[Resource], thankService: PostService) extends Actor {

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

case class DevCreatorConfig(creator: User, resource: Resource, tags: Set[Tag] = Set.empty[String])

trait DevUserDataService {

  def enable(configs: Seq[DevCreatorConfig]): Future[Boolean]

}


/**
  * Service that creates first users and integrations for testing UI and UX
  */
case class SimpleDevUserDataService @Inject()(
                                               userService: UserService,
                                               roService: ROService,
                                               postService: PostService,
                                               supPrjService: SupportedProjectService,
                                               eventBusManager: EventBusManager,
                                               implicit val ec: ExecutionContext
                                             ) extends DevUserDataService {

  // quotes, inspirational, motivational, cartoons, comics, webcomic, inspire, inspiring, art, poetry

  val resMap = List(
    DevCreatorConfig(
      User(
        id = IDGenerator.generate(),
        firstName = Some("Gavin"),
        lastName = Some("Than"),
        email = "gavin.than@example.com",
        profiles = Set(LoginInfo("patreon", "zenpencil")),
        avatar = Some("https://pbs.twimg.com/profile_images/493961823763181568/mb_2vK6y_400x400.jpeg"),
        link = Some("https://zenpencils.com")
      ),
      Resource.from("https://zenpencils.com"),
      Set("quotes", "inspirational", "motivational", "cartoons", "comics", "webcomic", "inspire", "inspiring", "art", "poetry")
    ),
    DevCreatorConfig(
      User(
        id = IDGenerator.generate(),
        firstName = Some("Manga"),
        lastName = Some("Stream"),
        email = "manga.stream@example.com",
        profiles = Set(LoginInfo("patreon", "mangastream")),
        avatar = Some("https://pbs.twimg.com/profile_images/544145066/twitterpic_400x400.png"),
        link = Some("https://readms.net")
      ),
      Resource.from("https://readms.net"),
      Set("manga", "japan", "one piece", "naruto", "bleach")
    ),
    DevCreatorConfig(
      User(
        id = IDGenerator.generate(),
        firstName = Some("Personal"),
        lastName = Some("Central"),
        avatar = Some("https://pbs.twimg.com/profile_images/741421578370572288/l1pjJGbp_400x400.jpg"),
        email = "personal.central@example.com",
        profiles = Set(LoginInfo("patreon", "personal.central")),
        link = Some("https://personacentral.com")
      ),
      Resource.from("https://personacentral.com"),
      Set("manga", "japan")
    )
  )

  val RESOURCES_TO_LOVE = List(
    Resource.from("https://zenpencils.com/comic/poison/"),
    Resource.from("https://zenpencils.com/comic/hustle/"),
    Resource.from("https://zenpencils.com/comic/poe/"),
  )

  enable(resMap)


  override def enable(configs: Seq[DevCreatorConfig]): Future[Boolean] = {
    (for {
      creators <- ensureCreators(configs.map(_.creator))
      tags <- ensureTags(creators.zip(configs.map(_.tags))) if (tags)
      resources <- ensureOwnership(creators.zip(configs.map(_.resource)))
    } yield {
      val allResources = resources ++ RESOURCES_TO_LOVE
      eventBusManager.onSignUp(Props(DevSignUpListener(allResources, postService)))
      eventBusManager.onLogin(Props(DevSignUpListener(allResources, postService)))
    }).recover({
      case t: Throwable => {
        print(t)
        System.exit(1)
        false
      }
    })
  }

  private def ensureCreators(creators: Seq[User]): Future[Seq[User]] = {
    val fCreators = for {
      creator <- creators
    } yield {
      userService.
        findByEmail(creator.email).
        flatMap({
          case Some(user) => Future.successful(user)
          case None => {
            userService.create(creator).map(creator => {
              eventBusManager.publish(SignUpEvent(creator, null))
              creator
            })
          }
        })
    }
    Future.sequence(fCreators)
  }

  private def ensureOwnership(creatorToRes: Seq[(User, Resource)]): Future[Seq[Resource]] = {
    val resources = for {
      (creator, resource) <- creatorToRes
    } yield {
      roService.
        assignOwnership(creator.id, resource).
        recoverWith({
          case _: Throwable =>
            Thread.sleep(10000)
            roService.assignOwnership(creator.id, resource)
        }
        )
    }
    Future.sequence(resources)
  }

  private def ensureTags(creatorToTags: Seq[(User, Set[Tag])]): Future[Boolean] = {
    val tags = for {
      (user, tags) <- creatorToTags
    } yield {
      supPrjService.assignTags(user.id, tags)
    }

    Future.sequence(tags).map(_.forall(_ == true))
  }

}
