package com.clemble.loveit.thank.service.repository

import com.clemble.loveit.common.model.{DibsProject, Email, EmailProject, OwnedProject, Project, Resource, UserID}
import com.clemble.loveit.thank.model.UserProjects

import scala.concurrent.Future

trait DibsProjectOwnershipRepository {

  def findDibsProject(user: UserID): Future[Seq[DibsProject]]

  def saveDibsProjects(user: UserID, projects: Seq[DibsProject]): Future[UserProjects]

  def validateDibsProject(user: UserID, url: Resource): Future[UserProjects]

  def deleteDibsProject(user: UserID, url: String): Future[UserProjects]

}

trait EmailProjectOwnershipRepository {

  def saveEmailProjects(user: UserID, projects: Seq[EmailProject]): Future[UserProjects]

  def validateEmailProject(user: UserID, email: Email): Future[UserProjects]

  def deleteEmailProject(user: UserID, email: Email): Future[UserProjects]

}

trait UserProjectsRepository extends ProjectRepository with DibsProjectOwnershipRepository with EmailProjectOwnershipRepository {

  def findById(user: UserID): Future[Option[UserProjects]]

  def save(projects: UserProjects): Future[UserProjects]

  def saveGoogleProjects(user: UserID, projects: Seq[OwnedProject]): Future[UserProjects]

  def saveTumblrProjects(user: UserID, projects: Seq[OwnedProject]): Future[UserProjects]

}
