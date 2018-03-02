package com.clemble.loveit.thank.service

import javax.inject.{Inject, Singleton}

import com.clemble.loveit.common.error.{RepositoryException, ResourceException}
import com.clemble.loveit.common.model._
import com.clemble.loveit.thank.model.{OwnedProjects, Project}
import com.clemble.loveit.thank.service.repository.ProjectRepository

import scala.concurrent.{ExecutionContext, Future}

trait ProjectService {

  def findById(project: ProjectID): Future[Option[Project]]

  def findProject(url: Resource): Future[Option[Project]]

  def findProjectsByUser(user: UserID): Future[List[Project]]

  def findOwned(user: UserID): Future[OwnedProjects]

  def refresh(user: UserID): Future[Seq[Project]]

  def update(project: Project): Future[Project]

}

@Singleton
class SimpleProjectService @Inject()(
                                      repo: ProjectRepository,
                                      enrichService: ProjectEnrichService,
                                      ownershipService: ProjectOwnershipService,
                                      implicit val ec: ExecutionContext
                                             ) extends ProjectService {

  override def findById(project: ProjectID): Future[Option[Project]] = {
    repo.findById(project)
  }

  override def findProject(url: Resource): Future[Option[Project]] = {
    repo.findProject(url)
  }

  override def findProjectsByUser(user: UserID): Future[List[Project]] = {
    repo.findProjectsByUser(user)
  }

  override def findOwned(user: UserID): Future[OwnedProjects] = {
    val fOwned = ownershipService.fetch(user)
    val fActive = repo.findProjectsByUser(user)
    for {
      installed <- fActive
      pending <- fOwned.map(_.filterNot(prj => installed.exists(_.url == prj.url)))
    } yield {
      OwnedProjects(pending, installed)
    }
  }

  override def refresh(user: UserID): Future[Seq[Project]] = {
    val fExisting = findProjectsByUser(user)
    val fEnriched = fExisting.map(_.map(enrichService.enrich)).flatMap(Future.sequence(_))

    for {
      owned <- ownershipService.fetch(user)
      existing <- fExisting
      newProjects = owned.filter(project => !existing.exists(_.url == project.url))
      _ <- Future.sequence(newProjects.map(repo.saveProject))
      enriched <- fEnriched
      updatedProjects = enriched.filterNot(existing.contains)
      _ <- Future.sequence(updatedProjects.map(update))
    } yield {
      newProjects ++ enriched
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

}
