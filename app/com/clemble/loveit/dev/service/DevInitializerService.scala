package com.clemble.loveit.dev.service

import java.time.YearMonth
import javax.inject.Inject

import com.clemble.loveit.auth.model.requests.RegistrationRequest
import com.clemble.loveit.common.model.{Resource, UserID}
import com.clemble.loveit.common.util.EventBusManager
import com.clemble.loveit.payment.model.EOMStatus
import com.clemble.loveit.payment.service.EOMPaymentService
import com.clemble.loveit.thank.model.{Post, Project}
import com.clemble.loveit.thank.service.PostService

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
        Project(
          user = "",
          resource = Resource.from("https://zenpencils.com"),
          title = Some("Zen Pencil"),
          avatar = Some("https://pbs.twimg.com/profile_images/493961823763181568/mb_2vK6y_400x400.jpeg"),
          tags = Set("quotes", "inspirational", "motivational", "cartoons", "comics", "webcomic", "inspire", "inspiring", "art", "poetry"),
          rss = Some("https://zenpencils.com/feed")
        ),
        Project(
          resource = Resource.from("https://personacentral.com"),
          title = Some("Personal Central"),
          user = "",
          avatar = Some("https://pbs.twimg.com/profile_images/741421578370572288/l1pjJGbp_400x400.jpg"),
          tags = Set("manga", "japan"),
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
        Project(
          resource = Resource.from("https://readms.net"),
          user = "",
          title = Some("Manga Stream"),
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
        Project(
          resource = Resource.from("https://www.sciencedaily.com"),
          user = "",
          title = Some("Science Daily"),
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
                                             ) extends DevInitializerService {

  enable(DevInitializerService.CREATORS, DevInitializerService.SUPPORTERS)


  override def enable(configs: Seq[DevCreatorConfig], supporters: Seq[RegistrationRequest]): Future[Boolean] = {
    (
      for {
        supporters <- supportersInitializer.initialize(supporters)
        posts <- creatorInitializer.initialize(configs)
        _ <- ensureLoveWasSpread(supporters, posts)
        _ <- ensureEOMProcessed()
      } yield {
        println("Dev user initialization finished")
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
      postService.thank(supporter, post.resource).map(_ => true)
    }
    Future.sequence(thanked).map(_.count(_ == true))
  }

  private def ensureEOMProcessed(): Future[Seq[EOMStatus]] = {
    val start = YearMonth.now().minusYears(1)
    val eomStatuses = for {
      i <- 1 to 12
    } yield {
      val yom = start.minusMonths(i)
      eomService.getStatus(yom).flatMap(_ match {
        case Some(status) => Future.successful(status)
        case None => eomService.run(yom)
      })
    }
    Future.sequence(eomStatuses)
  }

}
