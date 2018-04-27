package com.clemble.loveit.thank.service.repository

import com.clemble.loveit.common.model.{DibsProject, OwnedProject, Project, UserID}
import com.clemble.loveit.thank.model.UserProjects

import scala.concurrent.Future

trait UserProjectsRepository extends ProjectRepository {

  def findById(user: UserID): Future[Option[UserProjects]]

  def save(projects: UserProjects): Future[UserProjects]

  def saveDibsProjects(user: UserID, projects: Seq[DibsProject]): Future[UserProjects]

  def saveGoogleProjects(user: UserID, projects: Seq[OwnedProject]): Future[UserProjects]

  def saveTumblrProjects(user: UserID, projects: Seq[OwnedProject]): Future[UserProjects]

  def saveProject(project: Project): Future[Project]

  def deleteDibsProject(user: UserID, url: String): Future[UserProjects]

}
