package com.clemble.loveit.dev.service

import javax.inject.{Inject, Singleton}

import com.clemble.loveit.auth.model.requests.RegistrationRequest
import com.clemble.loveit.auth.service.{AuthService, UserLoggedIn, UserRegister}
import com.clemble.loveit.common.model.UserID
import com.clemble.loveit.common.util.EventBusManager
import com.clemble.loveit.thank.model.{Post, Project}
import com.clemble.loveit.thank.service.repository.ProjectRepository
import com.clemble.loveit.thank.service.{PostService, ProjectFeedService, ProjectService}
import com.mohiva.play.silhouette.api.{LoginEvent, SignUpEvent}

import scala.concurrent.{ExecutionContext, Future}

case class DevCreatorConfig(
                             creator: RegistrationRequest,
                             projects: Set[Project]
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
      assignedResources <- ensureCreatorsOwnership(creators.zip(configs.map(_.projects))) if (assignedResources)
      posts <- updateProjectsFeed(configs.flatMap(_.projects))
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

  private def ensureCreatorsOwnership(creatorToRes: Seq[(UserID, Set[Project])]): Future[Boolean] = {
    val resources = for {
      (creator, projects) <- creatorToRes
      project <- projects
    } yield {
      supPrjService
        .findProject(project.resource)
        .flatMap {
          case Some(_) => Future.successful(true)
          case None => prjRepo.saveProject(project.copy(user = creator))
        }
    }
    Future.sequence(resources).map(seq => seq.forall(_ == true))
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
