package com.clemble.loveit.dev.service

import javax.inject.Inject

import com.clemble.loveit.auth.model.requests.RegisterRequest
import com.clemble.loveit.auth.service.{AuthService, UserLoggedIn, UserRegister}
import com.clemble.loveit.common.model.{Resource, UserID}
import com.clemble.loveit.common.util.EventBusManager
import com.clemble.loveit.thank.model.{OpenGraphImage, OpenGraphObject, Post, SupportedProject}
import com.clemble.loveit.thank.service.{PostService, OwnedProjectService, SupportedProjectService}
import com.mohiva.play.silhouette.api._

import scala.concurrent.{ExecutionContext, Future}

case class DevCreatorConfig(
                             creator: RegisterRequest,
                             projects: Set[SupportedProject],
                             ogObjs: Set[OpenGraphObject]
                           )

trait DevUserDataService {

  def enable(configs: Seq[DevCreatorConfig]): Future[Boolean]

}


/**
  * Service that creates first users and integrations for testing UI and UX
  */
case class SimpleDevUserDataService @Inject()(
                                               authService: AuthService,
                                               roService: OwnedProjectService,
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
        //        link = Some("https://zenpencils.com")
      ),
      Set(
        SupportedProject(
          user = "",
          resource = Resource.from("https://zenpencils.com"),
          title = Some("Zen Pencil"),
          avatar = Some("https://pbs.twimg.com/profile_images/493961823763181568/mb_2vK6y_400x400.jpeg"),
          tags = Set("quotes", "inspirational", "motivational", "cartoons", "comics", "webcomic", "inspire", "inspiring", "art", "poetry")
        ),
        SupportedProject(
          user = "",
          resource = Resource.from("http://www.gocomics.com/zen-pencils"),
          title = Some("Zen Pencil on GoComics"),
          avatar = Some("http://avatar.amuniversal.com/feature_avatars/ubadge_images/features/ch/mid_u-201701251613.png"),
          tags = Set("quotes", "inspirational", "motivational", "cartoons", "comics", "webcomic", "inspire", "inspiring", "art", "poetry")
        )),

      Set(
        OpenGraphObject(
          url = "http://zenpencils.com/comic/creative/",
          title = Some("ZEN PENCILS » 221. 8 tips to be more creative by Zen Pencils"),
          description = Some("Today is the launch day of my new collection CREATIVE STRUGGLE: Illustrated Advice From Masters of Creativity! Besides including creative advice from greats like Einstein, Van Gogh, Curie and Hemingway, it also features an all-new comic by myself. The comic describes my eight tips to be more creativ…"),
          image = Some(OpenGraphImage(url = "https://cdn-zenpencils.netdna-ssl.com/wp-content/uploads/221_creativestruggle.jpg")),
          tags = Set("quotes", "inspirational", "motivational", "cartoons", "comics", "webcomic", "inspire", "inspiring", "art", "poetry")
        ),
        OpenGraphObject(
          url = "http://www.gocomics.com/zen-pencils",
          title = Some("Zen Pencils by Gavin Aung Than for Jan 29, 2018 | GoComics.com"),
          description = Some("Jan 29, 2018"),
          image = Some(
            OpenGraphImage(url = "http://assets.amuniversal.com/8b0ddf60d66601350cae005056a9545d", width = Some(900), height = Some(2545))
          )
        ),
        OpenGraphObject(
          url = "http://zenpencils.com/comic/hustle/",
          title = Some("ZEN PENCILS » 220. CHRIS GUILLEBEAU: The art of the Side Hustle"),
          image = Some(
            OpenGraphImage("https://cdn-zenpencils.netdna-ssl.com/wp-content/uploads/220_hustle.jpg")
          ),
        ),
        OpenGraphObject(
          url = "http://zenpencils.com/comic/poe/",
          image = Some(
            OpenGraphImage("https://cdn-zenpencils.netdna-ssl.com/wp-content/uploads/218_poe.jpg")),
          title = Some("ZEN PENCILS » 218. EDGAR ALLAN POE: Procrastination"),
          description = Some("Ah procrastination, something I constantly struggle with. Don’t we all? I’m fighting it right now – I don’t really enjoy writing these blog posts underneath each comic and I always put it off to until the last minute. I know I must do it, but I really don’t want to. As I sat down to write this, my c…"))
      )
    ),
    DevCreatorConfig(
      RegisterRequest(
        firstName = "Manga",
        lastName = "Stream",
        email = "manga.stream@example.com",
        password = "1234567890"
        //        id = IDGenerator.generate(),
        //        link = Some("https://readms.net")
      ),
      Set(
        SupportedProject(
          resource = Resource.from("https://readms.net"),
          user = "",
          title = Some("Manga Stream"),
          avatar = Some("https://pbs.twimg.com/profile_images/544145066/twitterpic_400x400.png"),
          tags = Set("manga", "japan", "one piece", "naruto", "bleach")
        )),
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
        //        link = Some("https://personacentral.com")
      ),
      Set(
        SupportedProject(
          resource = Resource.from("https://personacentral.com"),
          title = Some("Personal Central"),
          user = "",
          avatar = Some("https://pbs.twimg.com/profile_images/741421578370572288/l1pjJGbp_400x400.jpg"),
          tags = Set("manga", "japan")
        )
      ),
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
      assignedResources <- ensureOwnership(creators.zip(configs.map(_.projects)))
      posts <- ensurePosts(configs.flatMap(_.ogObjs))
    } yield {
      if (!assignedResources) {
        throw new IllegalArgumentException(s"Could not initialize resources")
      }
      assignedResources
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

  private def ensureOwnership(creatorToRes: Seq[(UserID, Set[SupportedProject])]): Future[Boolean] = {
    val resources = for {
      (creator, projects) <- creatorToRes
      project <- projects
    } yield {
      supPrjService
        .findProject(project.resource)
        .flatMap(_ match {
          case Some(_) => Future.successful(true)
          case None => roService.enable(project.copy(user = creator)).map(_ => true)
        })
    }
    Future.sequence(resources).map(seq => seq.forall(_ == true))
  }

  private def ensurePosts(posts: Seq[OpenGraphObject]): Future[Seq[Post]] = {
    Future.sequence(posts.map(obj => postService.create(obj)))
  }

}
