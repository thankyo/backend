package com.clemble.loveit.thank.service

import javax.inject.{Inject, Singleton}

import com.clemble.loveit.common.error.{RepositoryException, ResourceException}
import com.clemble.loveit.common.model._
import com.clemble.loveit.thank.model.Project
import com.clemble.loveit.thank.service.repository.{ProjectRepository}

import scala.concurrent.{ExecutionContext, Future}

trait ProjectService {

  def findById(project: ProjectID): Future[Option[Project]]

  def findProject(res: Resource): Future[Option[Project]]

  def findProjectsByUser(user: UserID): Future[List[Project]]

  def refresh(user: UserID): Future[List[Project]]

  def update(project: Project): Future[Project]

}

@Singleton
class SimpleProjectService @Inject()(
                                      repo: ProjectRepository,
                                      refreshService: OwnedProjectRefreshService,
                                      implicit val ec: ExecutionContext
                                             ) extends ProjectService {

  override def findById(project: ProjectID): Future[Option[Project]] = {
    repo.findById(project)
  }

  override def findProject(res: Resource): Future[Option[Project]] = {
    repo.findProject(res)
  }

  override def refresh(user: UserID): Future[List[Project]] = {
    val fExisting = findProjectsByUser(user)
    val fOwned = refreshService.fetch(user)

    val fNewProjects = for {
      existing <- fExisting
      owned <- fOwned
    } yield {
      owned.filter(project => !existing.exists(_.resource == project.resource))
    }

    val fNewSaved = fNewProjects
      .map(_.map(repo.saveProject))
      .flatMap(Future.sequence(_))
      .map(_.forall(_ == true))

    fNewSaved.flatMap(_ => findProjectsByUser(user))
  }

  override def update(project: Project): Future[Project] = {
    for {
      existingProjectOpt <- findProject(project.resource)
      _ = if (!existingProjectOpt.isDefined) throw ResourceException.noResourceExists()
      existingProject = existingProjectOpt.get
      _ = if (existingProject.user != project.user) throw ResourceException.differentOwner()
      _ = if (existingProject._id != project._id) throw ResourceException.differentId()
      updated <- repo.update(project)
      _ = if (!updated) throw RepositoryException.failedToUpdate()
    } yield {
      project
    }
  }

  override def findProjectsByUser(userID: UserID): Future[List[Project]] = {
    repo.findProjectsByUser(userID)
  }

}
