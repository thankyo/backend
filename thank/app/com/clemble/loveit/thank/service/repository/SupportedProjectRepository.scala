package com.clemble.loveit.thank.service.repository

import com.clemble.loveit.common.model.{ProjectID, Resource, Tag, UserID}
import com.clemble.loveit.thank.model.SupportedProject

import scala.concurrent.Future

trait SupportedProjectRepository {

  def findById(project: ProjectID): Future[Option[SupportedProject]]

  def findAll(ids: List[ProjectID]): Future[List[SupportedProject]]

  def findProject(res: Resource): Future[Option[SupportedProject]]

  def saveProject(project: SupportedProject): Future[Boolean]

  def update(project: SupportedProject): Future[Boolean]

  def assignTags(resource: Resource, tags: Set[Tag]): Future[Boolean]

  def findProjectsByUser(user: UserID): Future[List[SupportedProject]]

}
