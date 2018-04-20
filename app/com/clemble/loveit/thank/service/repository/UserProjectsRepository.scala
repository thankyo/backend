package com.clemble.loveit.thank.service.repository

import com.clemble.loveit.common.model.{OwnedProject, Project, UserID}
import com.clemble.loveit.thank.model.UserProjects

import scala.concurrent.Future

trait UserProjectsRepository {

  def findById(user: UserID): Future[Option[UserProjects]]

  def save(projects: UserProjects): Future[UserProjects]

  def append(user: UserID, owned: OwnedProject): Future[Boolean]

  def saveProject(project: Project): Future[Boolean]

}
