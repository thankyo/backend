package com.clemble.loveit.dev.service

import javax.inject.Inject

import akka.actor.{Actor, Props}
import com.clemble.loveit.auth.model.requests.RegisterRequest
import com.clemble.loveit.auth.service.{AuthService, UserLoggedIn, UserRegister}
import com.clemble.loveit.common.model.{Resource, Tag, UserID}
import com.clemble.loveit.common.util.EventBusManager
import com.clemble.loveit.thank.model.{OpenGraphImage, OpenGraphObject, Post}
import com.clemble.loveit.thank.service.{PostService, ROService, SupportedProjectService}
import com.clemble.loveit.user.model.User
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

case class DevCreatorConfig(creator: RegisterRequest, resource: Resource, tags: Set[Tag] = Set.empty[String], ogObjs: Set[OpenGraphObject])

trait DevUserDataService {

  def enable(configs: Seq[DevCreatorConfig]): Future[Boolean]

}


/**
  * Service that creates first users and integrations for testing UI and UX
  */
case class SimpleDevUserDataService @Inject()(
                                               authService: AuthService,
                                               roService: ROService,
                                               postService: PostService,
                                               supPrjService: SupportedProjectService,
                                               eventBusManager: EventBusManager,
                                               implicit val ec: ExecutionContext
                                             ) extends DevUserDataService {

  // quotes, inspirational, motivational, cartoons, comics, webcomic, inspire, inspiring, art, poetry

  val resMap = List(
    DevCreatorConfig(
      RegisterRequest(
        firstName = "Gavin",
        lastName = "Than",
        email = "gavin.than@example.com",
        password = "1234567890" //,
        //        id = IDGenerator.generate(),
        //        profiles = Set(LoginInfo("patreon", "zenpencil")),
        //        avatar = Some("https://pbs.twimg.com/profile_images/493961823763181568/mb_2vK6y_400x400.jpeg"),
        //        link = Some("https://zenpencils.com")
      ),
      Resource.from("https://zenpencils.com"),
      Set("quotes", "inspirational", "motivational", "cartoons", "comics", "webcomic", "inspire", "inspiring", "art", "poetry"),
      Set(
        OpenGraphObject(
          url = "http://zenpencils.com/comic/creative/",
          title = Some("ZEN PENCILS » 221. 8 tips to be more creative by Zen Pencils"),
          description = Some("Today is the launch day of my new collection CREATIVE STRUGGLE: Illustrated Advice From Masters of Creativity! Besides including creative advice from greats like Einstein, Van Gogh, Curie and Hemingway, it also features an all-new comic by myself. The comic describes my eight tips to be more creativ…"),
          image = Some(OpenGraphImage(url = "https://cdn-zenpencils.netdna-ssl.com/wp-content/uploads/221_creativestruggle.jpg")),
          tags = Set("quotes", "inspirational", "motivational", "cartoons", "comics", "webcomic", "inspire", "inspiring", "art", "poetry")
        )
      )
    ),
    DevCreatorConfig(
      RegisterRequest(
        firstName = "Manga",
        lastName = "Stream",
        email = "manga.stream@example.com",
        password = "1234567890"
        //        id = IDGenerator.generate(),
        //        profiles = Set(LoginInfo("patreon", "mangastream")),
        //        avatar = Some("https://pbs.twimg.com/profile_images/544145066/twitterpic_400x400.png"),
        //        link = Some("https://readms.net")
      ),
      Resource.from("https://readms.net"),
      Set("manga", "japan", "one piece", "naruto", "bleach"),
      Set(
        OpenGraphObject(
          url = "https://readms.net/r/one_piece/892/4843/1",
          title = Some("One Piece 892 - Manga Stream"),
          description = Some("Read free manga online like Naruto, Bleach, One Piece, Hunter x Hunter and many more."),
          image = Some(OpenGraphImage(url = "https://img.mangastream.com/cdn/manga/51/4843/01.png")),
          tags = Set("manga", "japan", "comics", "one piece")
        ),
        OpenGraphObject(
          url = "https://readms.net/r/attack_on_titan/101/4812/1",
          title = Some("Attack on Titan 101 - Manga Stream"),
          description = Some("Read free manga online like Naruto, Bleach, One Piece, Hunter x Hunter and many more."),
          image = Some(OpenGraphImage(url = "https://img.mangastream.com/cdn/manga/76/4812/01.png")),
          tags = Set("manga", "japan", "comics")
        )
      )
    ),
    DevCreatorConfig(
      RegisterRequest(
        firstName = "Personal",
        lastName = "Central",
        email = "personal.central@example.com",
        password = "1234567890"
        //        id = IDGenerator.generate(),
        //        avatar = Some("https://pbs.twimg.com/profile_images/741421578370572288/l1pjJGbp_400x400.jpg"),
        //        profiles = Set(LoginInfo("patreon", "personal.central")),
        //        link = Some("https://personacentral.com")
      ),
      Resource.from("https://personacentral.com"),
      Set("manga", "japan"),
      Set(
        OpenGraphObject(
          url = "https://personacentral.com/atlus-2018-online-consumer-survey-released/",
          title = Some("Atlus 2018 Online Consumer Survey Released, Includes Company Collaboration Questions - Persona Central"),
          description = Some("Atlus has released this year's online consumer survey to know more about what their customers want, including questions about remakes and company collaborations."),
          image = Some(OpenGraphImage(
            url = "https://personacentral.com/wp-content/uploads/2018/01/Persona-5-Dancing-Star-Night-Morgana.jpg"
          )),
          tags = Set("manga", "japan", "comics")
        )
      )
    )
  )

  enable(resMap)


  override def enable(configs: Seq[DevCreatorConfig]): Future[Boolean] = {
    (for {
      creators <- ensureCreators(configs.map(_.creator))
      tags <- ensureTags(creators.zip(configs.map(_.tags)))
      assignedResources <- ensureOwnership(creators.zip(configs.map(_.resource)))
      posts <- ensurePosts(configs.flatMap(_.ogObjs))
    } yield {
      if (!tags || !assignedResources) {
        throw new IllegalArgumentException(s"Could not initialize tags: ${tags}, resources: ${assignedResources}")
      }
      val allResources = posts.map(_.resource)
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

  private def ensureCreators(creators: Seq[RegisterRequest]): Future[Seq[UserID]] = {
    val fCreators = for {
      creator <- creators
    } yield {
      authService.register(creator).map(authRes => {
        authRes match {
          case UserRegister(user, _) =>
            eventBusManager.publish(SignUpEvent(user, null))
          case UserLoggedIn(user, _) =>
            eventBusManager.publish(LoginEvent(user, null))
        }
        authRes.user.id
      })
    }
    Future.sequence(fCreators)
  }

  private def ensureOwnership(creatorToRes: Seq[(UserID, Resource)]): Future[Boolean] = {
    val resources = for {
      (creator, resource) <- creatorToRes
    } yield {
      roService.assignOwnership(creator, resource)
    }
    Future
      .sequence(resources)
      .map((_) => true)
      .recover({ case _ => false })
  }

  private def ensureTags(creatorToTags: Seq[(UserID, Set[Tag])]): Future[Boolean] = {
    val tags = for {
      (creator, tags) <- creatorToTags
    } yield {
      supPrjService.assignTags(creator, tags)
    }

    Future.sequence(tags).map(_.forall(_ == true))
  }

  private def ensurePosts(posts: Seq[OpenGraphObject]): Future[Seq[Post]] = {
    Future.sequence(posts.map(obj => postService.create(obj)))
  }

}
