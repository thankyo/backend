package com.clemble.loveit.thank.service

import javax.inject.{Inject, Singleton}

import com.clemble.loveit.common.model.{ProjectID, Resource, UserID}
import com.clemble.loveit.thank.model.Project
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
    repo.findById(project)
  }

  override def findByUrl(url: Resource): Future[Option[Project]] = {
    repo.findByUrl(url)
  }

  override def findByUser(user: UserID): Future[List[Project]] = {
    repo.findByUser(user)
  }

}
