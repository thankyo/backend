package com.clemble.loveit.dev.service

import javax.inject.{Inject, Singleton}
import com.clemble.loveit.auth.model.requests.RegistrationRequest
import com.clemble.loveit.auth.service.{AuthService, UserLoggedIn, UserRegister}
import com.clemble.loveit.common.model.{Post, UserID}
import com.clemble.loveit.common.util.EventBusManager
import com.clemble.loveit.common.model.{OwnedProject, Project}
import com.clemble.loveit.thank.service.repository.ProjectRepository
import com.clemble.loveit.thank.service._
import com.mohiva.play.silhouette.api.{LoginEvent, SignUpEvent}

import scala.concurrent.{ExecutionContext, Future}

case class DevCreatorConfig(
  creator: RegistrationRequest,
  projects: Set[OwnedProject]
)

@Singleton
case class DevCreatorsInitializer @Inject()(
  feedService: ProjectFeedService,
  authService: AuthService,
  postService: PostService,
  prjLookupService: ProjectLookupService,
  prjService: ProjectService,
  usrPrjService: UserProjectsService,
  prjRepo: ProjectRepository,
  eventBusManager: EventBusManager,
  implicit val ec: ExecutionContext
) {

  def initialize(configs: Seq[DevCreatorConfig]): Future[Seq[Post]] = {
    for {
      creators <- ensureUserExist(configs.map(_.creator))
      projects <- ensureCreatorsOwnership(creators.zip(configs.map(_.projects)))
      posts <- updateProjectsFeed(projects)
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

  private def ensureCreatorsOwnership(creatorToProjects: Seq[(UserID, Set[OwnedProject])]): Future[Seq[Project]] = {
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
              _ <- usrPrjService.dibsOnUrl(creator, project.url)
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
