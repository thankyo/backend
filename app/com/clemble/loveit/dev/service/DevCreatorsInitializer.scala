package com.clemble.loveit.dev.service

import javax.inject.{Inject, Singleton}

import com.clemble.loveit.auth.model.requests.RegistrationRequest
import com.clemble.loveit.auth.service.{AuthService, UserLoggedIn, UserRegister}
import com.clemble.loveit.common.model.UserID
import com.clemble.loveit.common.util.EventBusManager
import com.clemble.loveit.thank.model.{Post, Project, ProjectConstructor}
import com.clemble.loveit.thank.service.repository.ProjectRepository
import com.clemble.loveit.thank.service.{PostService, ProjectFeedService, ProjectService}
import com.mohiva.play.silhouette.api.{LoginEvent, SignUpEvent}

import scala.concurrent.{ExecutionContext, Future}

case class DevCreatorConfig(
                             creator: RegistrationRequest,
                             projects: Set[ProjectConstructor]
                           )

@Singleton
case class DevCreatorsInitializer @Inject()(
                                             feedService: ProjectFeedService,
                                             authService: AuthService,
                                             postService: PostService,
                                             supPrjService: ProjectService,
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

  private def ensureCreatorsOwnership(creatorToProjects: Seq[(UserID, Set[ProjectConstructor])]): Future[Seq[Project]] = {
    val resources = for {
      (creator, projects) <- creatorToProjects
      project <- projects
    } yield {
      supPrjService
        .findProject(project.url)
        .flatMap {
          case Some(prj) => Future.successful(prj)
          case None => supPrjService.create(creator, project)
        }
    }
    Future.sequence(resources)
  }

  private def updateProjectsFeed(projects: Seq[Project]): Future[Seq[Post]] = {
    val refreshedProjects = for {
      project <- projects
    } yield {
      feedService.refresh(project).recoverWith({ case _ => Future.successful(List.empty)})
    }
    Future.sequence(refreshedProjects).map(_.flatten)
  }

}
