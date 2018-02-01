package com.clemble.loveit.thank.service.repository

import com.clemble.loveit.common.model.{ProjectID, Resource, Tag, UserID}
import com.clemble.loveit.thank.model.{Post, SupportedProject}

import scala.concurrent.Future

trait PostRepository {

  def save(post: Post): Future[Boolean]

  def assignTags(res: Resource, tags: Set[Tag]): Future[Boolean]

  def update(post: Post): Future[Boolean]

  def findByResource(uri: Resource): Future[Option[Post]]

  def findByTags(tags: Set[String]): Future[List[Post]]

  def findByAuthor(author: UserID): Future[List[Post]]

  def findByProject(project: ProjectID): Future[List[Post]]

  def updateProject(project: SupportedProject): Future[Boolean]

  def markSupported(user: String, url: Resource): Future[Boolean]

  def isSupportedBy(user: UserID, resource: Resource): Future[Option[Boolean]]

}
