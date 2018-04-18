package com.clemble.loveit.thank.service

import javax.inject.{Inject, Singleton}
import com.clemble.loveit.common.error.{RepositoryException, ResourceException}
import com.clemble.loveit.common.model._
import com.clemble.loveit.thank.model.UserProjects
import com.clemble.loveit.thank.service.repository.ProjectRepository

import scala.concurrent.{ExecutionContext, Future}

trait ProjectService {

  def findProjectsByUser(user: UserID): Future[List[Project]]

  def getOwned(user: UserID): Future[UserProjects]

  def create(user: UserID, project: ProjectConstructor): Future[Project]

  def delete(user: UserID, id: ProjectID): Future[Boolean]

  def update(project: Project): Future[Project]

}

@Singleton
class SimpleProjectService @Inject()(
  repo: ProjectRepository,
  postService: PostService,
  ownershipService: ProjectOwnershipService,
  verificationService: ProjectOwnershipVerificationService,
  implicit val ec: ExecutionContext
) extends ProjectService {

  override def findProjectsByUser(user: UserID): Future[List[Project]] = {
    repo.findByUser(user)
  }

  override def getOwned(user: UserID): Future[UserProjects] = {
    val fOwned = ownershipService.fetch(user)
    val fActive = repo.findByUser(user)
    for {
      installed <- fActive
      pending <- fOwned
    } yield {
      UserProjects(pending, installed)
    }
  }


  override def create(user: UserID, project: ProjectConstructor): Future[Project] = {
    for {
      existingProjectOpt <- repo.findByUrl(project.url)
      _ = if (existingProjectOpt.isDefined) throw ResourceException.projectAlreadyCreated()
      verification <- verificationService.verify(user, project.url)
      save <- repo.save(Project.from(user, project.copy(verification = verification)))
    } yield {
      save
    }
  }

  override def update(project: Project): Future[Project] = {
    for {
      existingProjectOpt <- repo.findByUrl(project.url)
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
      removedPosts <- postService.delete(projectOpt.get)
    } yield {
      removed && removedPosts
    }
  }

}
