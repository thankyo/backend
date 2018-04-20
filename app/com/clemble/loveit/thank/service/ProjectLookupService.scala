package com.clemble.loveit.thank.service

import javax.inject.{Inject, Singleton}
import com.clemble.loveit.common.model.{Project, ProjectID, Resource, UserID}
import com.clemble.loveit.thank.service.repository.ProjectRepository

import scala.concurrent.Future

trait ProjectLookupService {

  def findById(project: ProjectID): Future[Option[Project]]

  def findByUrl(url: Resource): Future[Option[Project]]

  def findByUser(user: UserID): Future[List[Project]]

}

@Singleton
case class SimpleProjectLookupService @Inject() (repo: ProjectRepository) extends ProjectLookupService {

  override def findById(project: ProjectID): Future[Option[Project]] = {
    repo.findProjectById(project)
  }

  override def findByUrl(url: Resource): Future[Option[Project]] = {
    repo.findProjectByUrl(url)
  }

  override def findByUser(user: UserID): Future[List[Project]] = {
    repo.findProjectsByUser(user)
  }

}
