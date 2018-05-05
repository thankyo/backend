package com.clemble.loveit.thank.service.repository

import com.clemble.loveit.common.model.{Project, ProjectID, ProjectLike, Resource, Tag, UserID}

import scala.concurrent.Future

trait ProjectRepository {

  def findProjectById(project: ProjectID): Future[Option[Project]]

  def findProjectByUrl(url: Resource): Future[Option[Project]]

  def findProjectsByUser(user: UserID): Future[List[Project]]

  def findAllProjects(ids: List[ProjectID]): Future[List[Project]]

  def saveProject(project: Project): Future[Project]

  def updateProject(project: Project): Future[Boolean]

  def deleteProject(user: UserID, id: ProjectID): Future[Boolean]

}
