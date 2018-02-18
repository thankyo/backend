package com.clemble.loveit.dev.service

import java.time.YearMonth
import javax.inject.Inject

import com.clemble.loveit.auth.model.requests.RegisterRequest
import com.clemble.loveit.common.model.{Resource, UserID}
import com.clemble.loveit.common.util.EventBusManager
import com.clemble.loveit.payment.model.EOMStatus
import com.clemble.loveit.payment.service.EOMPaymentService
import com.clemble.loveit.thank.model.{Post, Project}
import com.clemble.loveit.thank.service.PostService

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Random

trait DevUserDataService {

  def enable(configs: Seq[DevCreatorConfig], supporters: Seq[RegisterRequest]): Future[Boolean]

}

object DevUserDataService {

  val CREATORS = List(
    DevCreatorConfig(
      RegisterRequest(
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
      RegisterRequest(
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
    RegisterRequest(
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
case class SimpleDevUserDataService @Inject()(
                                               creatorInitializer: DevCreatorsInitializer,
                                               supportersInitializer: DevSupportersInitializer,
                                               postService: PostService,
                                               eomService: EOMPaymentService,
                                               eventBusManager: EventBusManager,
                                               implicit val ec: ExecutionContext
                                             ) extends DevUserDataService {

  enable(DevUserDataService.CREATORS, DevUserDataService.SUPPORTERS)


  override def enable(configs: Seq[DevCreatorConfig], supporters: Seq[RegisterRequest]): Future[Boolean] = {
    (
      for {
        supporters <- supportersInitializer.initialize(supporters)
        posts <- creatorInitializer.initialize(configs)
        _ <- ensureLoveWasSpread(supporters, posts)
        _ <- ensureEOMProcessed()
      } yield {
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
      if (Random.nextBoolean()) {
        postService.thank(supporter, post.resource).map(_ => true)
      } else {
        Future.successful(false)
      }
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
