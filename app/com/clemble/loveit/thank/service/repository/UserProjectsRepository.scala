package com.clemble.loveit.thank.service.repository

import com.clemble.loveit.common.model.{OwnedProject, Project, UserID}
import com.clemble.loveit.thank.model.UserProjects

import scala.concurrent.Future

trait UserProjectsRepository extends ProjectRepository {

  def findById(user: UserID): Future[Option[UserProjects]]

  def save(projects: UserProjects): Future[UserProjects]

  def saveOwnedProject(user: UserID, owned: Seq[OwnedProject]): Future[UserProjects]

  def saveProject(project: Project): Future[Project]

}
