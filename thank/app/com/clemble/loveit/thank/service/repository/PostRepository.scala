package com.clemble.loveit.thank.service.repository

import com.clemble.loveit.common.model.{Resource, UserID}
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

  def update(post: Post): Future[Boolean]

  /**
    * Checks if user thanked resource
    */
  def isSupportedBy(user: UserID, resource: Resource): Future[Option[Boolean]]

  /**
    * Find [[Post]]
    *
    * @return existing or creates new [[Post]]
    */
  def findByResource(uri: Resource): Future[Option[Post]]

  /**
    * Increases number of supporters given
    *
    * @return true, if update passed
    */
  def markSupported(user: String, url: Resource): Future[Boolean]

  /**
    * Updates current resource ownership and all it's children
    */
  def updateOwner(owner: SupportedProject, url: Resource): Future[Boolean]

}
