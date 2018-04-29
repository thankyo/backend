package com.clemble.loveit.dev.service

import javax.inject.{Inject, Singleton}
import com.clemble.loveit.auth.model.requests.RegistrationRequest
import com.clemble.loveit.auth.service.{AuthService, UserLoggedIn, UserRegister}
import com.clemble.loveit.common.model.{DibsProject, OwnedProject, Post, Project, UserID}
import com.clemble.loveit.common.util.EventBusManager
import com.clemble.loveit.thank.service.repository.ProjectRepository
import com.clemble.loveit.thank.service._
import com.mohiva.play.silhouette.api.{Logger, LoginEvent, SignUpEvent}

import scala.concurrent.{ExecutionContext, Future}

case class DevCreatorConfig(
  creator: RegistrationRequest,
  projects: Set[DibsProject]
)

@Singleton
case class DevCreatorsInitializer @Inject()(
  feedService: ProjectFeedService,
  authService: AuthService,
  postService: PostService,
  prjLookupService: ProjectLookupService,
  prjService: ProjectService,
  dibsOwnSvc: DibsProjectOwnershipService,
  usrPrjService: UserProjectsService,
  prjRepo: ProjectRepository,
  eventBusManager: EventBusManager,
  implicit val ec: ExecutionContext
) extends Logger {

  def initialize(configs: Seq[DevCreatorConfig]): Future[Seq[Post]] = {
    for {
      creators <- ensureUserExist(configs.map(_.creator))
      _ = logger.info("Creators initialized")
      projects <- ensureCreatorsOwnership(creators.zip(configs.map(_.projects)))
      _ = logger.info("Project ownership verified")
      posts <- updateProjectsFeed(projects)
      _ = logger.info("Posts fetched from the feed")
    } yield {
      posts
    }
  }

  private def ensureUserExist(creators: Seq[RegistrationRequest]): Future[Seq[UserID]] = {
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

  private def ensureCreatorsOwnership(creatorToProjects: Seq[(UserID, Set[DibsProject])]): Future[Seq[Project]] = {
    val resources = for {
      (creator, projects) <- creatorToProjects
      project <- projects
    } yield {
      prjLookupService
        .findByUrl(project.url)
        .flatMap {
          case Some(prj) => Future.successful(prj)
          case None =>
            for {
              _ <- dibsOwnSvc.dibs(creator, project.url)
              project <- prjService.create(creator, project)
            } yield {
              project
            }
        }
    }
    Future.sequence(resources)
  }

  private def updateProjectsFeed(projects: Seq[Project]): Future[Seq[Post]] = {
    val refreshedProjects = for {
      project <- projects
    } yield {
      feedService.refresh(project).recoverWith({ case _ => Future.successful(List.empty) })
    }
    Future.sequence(refreshedProjects).map(_.flatten)
  }

}
