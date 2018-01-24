package com.clemble.loveit.thank.service.repository

import com.clemble.loveit.common.model.{Resource, Tag, UserID}
import com.clemble.loveit.thank.model.{Post, SupportedProject}

import scala.concurrent.Future

/**
  * [[Post]] repository
  */
trait PostRepository {

  /**
    * Create appropriate url
    *
    * @param post object to create
    * @return true if create was a success
    */
  def save(post: Post): Future[Boolean]

  def assignTags(res: Resource, tags: Set[Tag]): Future[Boolean]

  def update(post: Post): Future[Boolean]

  /**
    * Find [[Post]]
    *
    * @return existing or creates new [[Post]]
    */
  def findByResource(uri: Resource): Future[Option[Post]]

  /**
    * Updates current resource ownership and all it's children
    */
  def updateOwner(owner: SupportedProject, url: Resource): Future[Boolean]

  /**
    * Increases number of supporters given
    *
    * @return true, if update passed
    */
  def markSupported(user: String, url: Resource): Future[Boolean]

  /**
    * Checks if user thanked resource
    */
  def isSupportedBy(user: UserID, resource: Resource): Future[Option[Boolean]]

}
