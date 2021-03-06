package com.clemble.loveit.thank.service

import javax.inject.{Inject, Singleton}
import com.clemble.loveit.common.error.{RepositoryException, ResourceException}
import com.clemble.loveit.common.model._
import com.clemble.loveit.thank.service.repository.{UserProjectRepository}

import scala.concurrent.{ExecutionContext, Future}

trait ProjectService {

  def create(user: UserID, project: ProjectLike): Future[Project]

  def delete(user: UserID, id: ProjectID): Future[Boolean]

  def update(project: Project): Future[Project]

}

@Singleton
class SimpleProjectService @Inject()(
  repo: UserProjectRepository,
  postService: PostService,
  implicit val ec: ExecutionContext
) extends ProjectService {

  override def create(user: UserID, project: ProjectLike): Future[Project] = {
    for {
      existingProjectOpt <- repo.findProjectByUrl(project.url)
      _ = if (existingProjectOpt.isDefined) throw ResourceException.projectAlreadyCreated()
      save <- repo.saveProject(Project.from(user, project))
    } yield {
      save
    }
  }

  override def update(project: Project): Future[Project] = {
    for {
      existingProjectOpt <- repo.findProjectById(project._id)
      _ = if (!existingProjectOpt.isDefined) throw ResourceException.noResourceExists()
      existingProject = existingProjectOpt.get
      _ = if (existingProject.user != project.user) throw ResourceException.differentOwner()
      _ = if (existingProject.url != project.url) throw ResourceException.urlModified()
      updated <- repo.updateProject(project)
      _ = if (!updated) throw RepositoryException.failedToUpdate()
    } yield {
      project
    }
  }

  override def delete(user: UserID, id: ProjectID): Future[Boolean] = {
    for {
      projectOpt <- repo.findProjectById(id)
      _ = if (!projectOpt.isDefined) throw ResourceException.noResourceExists()
      _ = if (!projectOpt.forall(_.user == user)) throw ResourceException.differentOwner()
      removed <- repo.deleteProject(user, id)
      removedPosts <- postService.delete(projectOpt.get)
    } yield {
      removed && removedPosts
    }
  }

}
