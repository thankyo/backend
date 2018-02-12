package com.clemble.loveit.thank.service

import javax.inject.{Inject, Singleton}

import com.clemble.loveit.common.error.{RepositoryException, ResourceException}
import com.clemble.loveit.common.model._
import com.clemble.loveit.thank.model.SupportedProject
import com.clemble.loveit.thank.service.repository.{SupportedProjectRepository}

import scala.concurrent.{ExecutionContext, Future}

trait SupportedProjectService {

  def findById(project: ProjectID): Future[Option[SupportedProject]]

  def findProject(res: Resource): Future[Option[SupportedProject]]

  def findProjectsByUser(user: UserID): Future[List[SupportedProject]]

  def create(project: SupportedProject): Future[Boolean]

  def update(project: SupportedProject): Future[SupportedProject]

}

@Singleton
class SimpleSupportedProjectService @Inject()(
                                               repo: SupportedProjectRepository,
                                               supTrackRepo: SupportedProjectTrackService,
                                               implicit val ec: ExecutionContext
                                             ) extends SupportedProjectService {

  override def findById(project: ProjectID): Future[Option[SupportedProject]] = {
    repo.findById(project)
  }

  override def findProject(res: Resource): Future[Option[SupportedProject]] = {
    repo.findProject(res)
  }

  override def create(project: SupportedProject): Future[Boolean] = {
    repo.saveProject(project)
  }

  override def update(project: SupportedProject): Future[SupportedProject] = {
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

  override def findProjectsByUser(userID: UserID): Future[List[SupportedProject]] = {
    repo.findProjectsByUser(userID)
  }

}
