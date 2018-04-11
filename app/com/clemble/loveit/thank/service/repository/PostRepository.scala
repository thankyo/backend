package com.clemble.loveit.thank.service.repository

import com.clemble.loveit.common.model.{Post, PostID, Project, ProjectID, Resource, Tag, UserID}
import com.clemble.loveit.common.model.Project

import scala.concurrent.Future

trait PostRepository {

  def save(post: Post): Future[Boolean]

  def update(post: Post): Future[Option[Post]]

  def delete(id: PostID): Future[Option[Post]]

  def deleteAll(user: UserID, url: Resource): Future[Boolean]

  def findById(id: PostID): Future[Option[Post]]

  def findByResource(url: Resource): Future[Option[Post]]

  def findByTags(tags: Set[String]): Future[List[Post]]

  def findByAuthor(author: UserID): Future[List[Post]]

  def findByProject(project: ProjectID): Future[List[Post]]

  def updateProject(project: Project): Future[Boolean]

  def markSupported(user: String, url: Resource): Future[Boolean]

  def isSupportedBy(user: UserID, url: Resource): Future[Option[Boolean]]

}
