package com.clemble.loveit.dev.service

import java.time.YearMonth

import javax.inject.Inject
import com.clemble.loveit.auth.model.requests.RegistrationRequest
import com.clemble.loveit.common.model.{DibsVerification, OwnedProject, Post, Project, UserID}
import com.clemble.loveit.common.util.EventBusManager
import com.clemble.loveit.payment.model.EOMStatus
import com.clemble.loveit.payment.service.EOMPaymentService
import com.clemble.loveit.thank.service.PostService
import com.mohiva.play.silhouette.api.Logger

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Random

trait DevInitializerService {

  def enable(configs: Seq[DevCreatorConfig], supporters: Seq[RegistrationRequest]): Future[Boolean]

}

object DevInitializerService {

  val CREATORS = List(
    DevCreatorConfig(
      RegistrationRequest(
        firstName = "Gavin",
        lastName = "Than",
        email = "gavin.than@example.com",
        password = "1234567890",
      ),
      Set(
        OwnedProject(
          url = "https://zenpencils.com",
          title = "Zen Pencil",
          shortDescription = "My personal blog",
          description = Some("Personal blog for zenpencils"),
          verification = DibsVerification,
          avatar = Some("https://pbs.twimg.com/profile_images/493961823763181568/mb_2vK6y_400x400.jpeg"),
          tags = Set("quotes", "inspirational", "motivational", "cartoons", "comics", "webcomic", "inspire", "inspiring", "art", "poetry"),
          rss = Some("https://zenpencils.com/feed")
        ),
        OwnedProject(
          url = "https://personacentral.com",
          title = "Personal Central",
          shortDescription = "My side japan project",
          description = Some("Once I was in Japan"),
          avatar = Some("https://pbs.twimg.com/profile_images/741421578370572288/l1pjJGbp_400x400.jpg"),
          tags = Set("manga", "japan"),
          verification = DibsVerification,
          rss = Some("https://personacentral.com/feed")
        )
      )
    ),
    DevCreatorConfig(
      RegistrationRequest(
        firstName = "Manga",
        lastName = "Stream",
        email = "manga.stream@example.com",
        password = "1234567890"
      ),
      Set(
        OwnedProject(
          url = "https://readms.net",
          title = "Manga Stream",
          shortDescription = "Awesome scanlation group",
          verification = DibsVerification,
          description = Some("We do what we do, because we love it"),
          avatar = Some("https://pbs.twimg.com/profile_images/544145066/twitterpic_400x400.png"),
          tags = Set("manga", "japan", "one piece", "naruto", "bleach"),
          rss = Some("https://readms.net/rss")
        )),
    ),
    DevCreatorConfig(
      RegistrationRequest(
        firstName = "Science",
        lastName = "Daily",
        email = "science.daily@example.com",
        password = "1234567890"
      ),
      Set(
        OwnedProject(
          url = "https://www.sciencedaily.com",
          title = "Science Daily Blog",
          shortDescription = "Science Daily Blog",
          verification = DibsVerification,
          description = Some("This is a small blog for science hungry people"),
          avatar = Some("https://www.sciencedaily.com/images/sd-logo.png"),
          tags = Set("science", "daily", "tech"),
          rss = Some("https://www.sciencedaily.com/rss/top/science.xml")
        )
      )
    )
  )


  val POSSIBLE_NAME = List(
    "James", "Mary",
    "John", "Patricia",
    "Robert", "Jennifer",
    "Michael", "Elizabeth",
    "William", "Linda",
    "David", "Barbara",
    "Richard", "Susan",
    "Joseph", "Jessica",
    "Thomas", "Margaret"
  )

  val SUPPORTERS = 1 to 100 map (i => {
    val firstName = POSSIBLE_NAME(Random.nextInt(POSSIBLE_NAME.length))
    val lastName = POSSIBLE_NAME(Random.nextInt(POSSIBLE_NAME.length))
    RegistrationRequest(
      firstName = firstName,
      lastName = lastName,
      email = s"${i}@example.com",
      password = "1234567890"
    )
  })

}

/**
  * Service that creates first users and integrations for testing UI and UX
  */
case class SimpleDevInitializerService @Inject()(
                                               creatorInitializer: DevCreatorsInitializer,
                                               supportersInitializer: DevSupportersInitializer,
                                               postService: PostService,
                                               eomService: EOMPaymentService,
                                               eventBusManager: EventBusManager,
                                               implicit val ec: ExecutionContext
                                             ) extends DevInitializerService with Logger {

  enable(DevInitializerService.CREATORS, DevInitializerService.SUPPORTERS)


  val tasks = List(

  )

  override def enable(configs: Seq[DevCreatorConfig], supporters: Seq[RegistrationRequest]): Future[Boolean] = {
    (
      for {
        supporters <- supportersInitializer.initialize(supporters)
        _ = logger.info(s"Supporters initialization done ${supporters.length}")
        posts <- if(supporters.isEmpty) Future.successful(List.empty) else creatorInitializer.initialize(configs)
        _ = logger.info("Creators initialization done")
        thanks <- if (posts.isEmpty) Future.successful(0) else ensureLoveWasSpread(supporters, posts)
        _ = logger.info(s"Love was spread ${thanks}")
        statusOpt <- if (supporters.isEmpty) Future.successful(None) else ensureEOMProcessed()
        _ = logger.info(s"EOM processing was run with ${statusOpt}")
      } yield {
        logger.info("Dev user initialization finished")
        true
      }).recover({
      case t => {
        print(t)
        System.exit(1)
        ???
      }
    })
  }

  private def ensureLoveWasSpread(supporters: Seq[UserID], posts: Seq[Post]): Future[Int] = {
    val thanked = for {
      supporter <- supporters
      post <- posts
    } yield {
      postService.thank(supporter, post.url).map(_ => true)
    }
    Future.sequence(thanked).map(_.count(_ == true))
  }

  private def ensureEOMProcessed(): Future[Option[EOMStatus]] = {
    val yom = YearMonth.now().minusMonths(1)
    eomService.getStatus(yom).flatMap({
      case Some(status) => Future.successful(Some(status))
      case None => eomService.run(yom).map(Some(_))
    })
  }

}
