package com.clemble.loveit.thank.service.repository

import com.clemble.loveit.common.model.{ProjectID, Resource, Tag, UserID}
import com.clemble.loveit.thank.model.Project

import scala.concurrent.Future

trait ProjectRepository {

  def findById(project: ProjectID): Future[Option[Project]]

  def findAll(ids: List[ProjectID]): Future[List[Project]]

  def findProject(url: Resource): Future[Option[Project]]

  def saveProject(project: Project): Future[Boolean]

  def update(project: Project): Future[Boolean]

  def assignTags(url: Resource, tags: Set[Tag]): Future[Boolean]

  def findProjectsByUser(user: UserID): Future[List[Project]]

}
