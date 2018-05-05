package com.clemble.loveit.thank.service.repository

import com.clemble.loveit.common.model.{DibsProject, Email, EmailProject, OwnedProject, Project, Resource, UserID}
import com.clemble.loveit.thank.model.UserProject

import scala.concurrent.Future

trait DibsProjectOwnershipRepository {

  def findDibsProject(user: UserID): Future[Seq[DibsProject]]

  def saveDibsProjects(user: UserID, projects: Seq[DibsProject]): Future[UserProject]

  def validateDibsProject(user: UserID, url: Resource): Future[UserProject]

  def deleteDibsProject(user: UserID, url: String): Future[UserProject]

}

trait EmailProjectOwnershipRepository {

  def saveEmailProjects(user: UserID, projects: Seq[EmailProject]): Future[UserProject]

  def validateEmailProject(user: UserID, email: Email): Future[UserProject]

  def deleteEmailProject(user: UserID, email: Email): Future[UserProject]

}

trait UserProjectsRepository extends ProjectRepository with DibsProjectOwnershipRepository with EmailProjectOwnershipRepository {

  def findAll(): Future[List[UserProject]]

  def findById(user: UserID): Future[Option[UserProject]]

  def save(projects: UserProject): Future[UserProject]

  def saveGoogleProjects(user: UserID, projects: Seq[OwnedProject]): Future[UserProject]

  def saveTumblrProjects(user: UserID, projects: Seq[OwnedProject]): Future[UserProject]

}
