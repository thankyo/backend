package com.clemble.loveit.thank.service

import javax.inject.{Inject, Singleton}

import com.clemble.loveit.common.error.{RepositoryException, ResourceException}
import com.clemble.loveit.common.model._
import com.clemble.loveit.common.util.IDGenerator
import com.clemble.loveit.thank.model.{OwnedProjects, Project}
import com.clemble.loveit.thank.service.repository.ProjectRepository

import scala.concurrent.{ExecutionContext, Future}

trait ProjectService {

  def findById(project: ProjectID): Future[Option[Project]]

  def findProject(url: Resource): Future[Option[Project]]

  def findProjectsByUser(user: UserID): Future[List[Project]]

  def getOwned(user: UserID): Future[OwnedProjects]

  def create(project: Project): Future[Project]

  def delete(user: UserID, id: ProjectID): Future[Boolean]

  def update(project: Project): Future[Project]

}

@Singleton
class SimpleProjectService @Inject()(
                                      repo: ProjectRepository,
                                      ownershipService: ProjectOwnershipService,
                                      implicit val ec: ExecutionContext
                                             ) extends ProjectService {

  override def findById(project: ProjectID): Future[Option[Project]] = {
    repo.findById(project)
  }

  override def findProject(url: Resource): Future[Option[Project]] = {
    repo.findByUrl(url)
  }

  override def findProjectsByUser(user: UserID): Future[List[Project]] = {
    repo.findByUser(user)
  }

  override def getOwned(user: UserID): Future[OwnedProjects] = {
    val fOwned = ownershipService.fetch(user)
    val fActive = repo.findByUser(user)
    for {
      installed <- fActive
      pending <- fOwned.map(_.filterNot(ownedUrl => installed.exists(_.url == ownedUrl)))
    } yield {
      OwnedProjects(pending, installed)
    }
  }


  override def create(project: Project): Future[Project] = {
    for {
      existingProjectOpt <- findProject(project.url)
      _ = if (existingProjectOpt.isDefined) throw ResourceException.projectAlreadyCreated()
      save <- repo.save(project.copy(_id = IDGenerator.generate()))
    } yield {
      save
    }
  }

  override def update(project: Project): Future[Project] = {
    for {
      existingProjectOpt <- findProject(project.url)
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

  override def delete(user: UserID, id: ProjectID): Future[Boolean] = {
    for {
      projectOpt <- repo.findById(id)
      _ = if (!projectOpt.isDefined) throw ResourceException.noResourceExists()
      _ = if (!projectOpt.forall(_.user == user)) throw ResourceException.differentOwner()
      removed <- repo.delete(id)
    } yield {
      removed
    }
  }

}
