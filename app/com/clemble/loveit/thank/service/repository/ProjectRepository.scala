package com.clemble.loveit.thank.service.repository

import com.clemble.loveit.common.model.{Project, ProjectID, Resource, Tag, UserID}

import scala.concurrent.Future

trait ProjectRepository {

  def findById(project: ProjectID): Future[Option[Project]]

  def findAll(ids: List[ProjectID]): Future[List[Project]]

  def findByUrl(url: Resource): Future[Option[Project]]

  def findByUser(user: UserID): Future[List[Project]]

  def save(project: Project): Future[Project]

  def update(project: Project): Future[Boolean]

  def delete(id: ProjectID): Future[Boolean]

}
