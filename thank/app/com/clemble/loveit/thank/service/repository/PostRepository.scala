package com.clemble.loveit.thank.service.repository

import com.clemble.loveit.common.model.{ProjectID, Resource, Tag, UserID}
import com.clemble.loveit.thank.model.{Post, Project}

import scala.concurrent.Future

trait PostRepository {

  def save(post: Post): Future[Boolean]

  def assignTags(url: Resource, tags: Set[Tag]): Future[Boolean]

  def update(post: Post): Future[Boolean]

  def deleteAll(user: UserID, url: Resource): Future[Boolean]

  def findById(id: String): Future[Option[Post]]

  def findByResource(url: Resource): Future[Option[Post]]

  def findByTags(tags: Set[String]): Future[List[Post]]

  def findByAuthor(author: UserID): Future[List[Post]]

  def findByProject(project: ProjectID): Future[List[Post]]

  def updateProject(project: Project): Future[Boolean]

  def markSupported(user: String, url: Resource): Future[Boolean]

  def isSupportedBy(user: UserID, url: Resource): Future[Option[Boolean]]

}
