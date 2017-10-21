package com.clemble.loveit.thank.service.repository

import com.clemble.loveit.common.model.{Resource, UserID}
import com.clemble.loveit.thank.model.{SupportedProject, Thank}

import scala.concurrent.Future

/**
  * [[Thank]] repository
  */
trait ThankRepository {

  /**
    * Create appropriate url
    *
    * @param thank object to create
    * @return true if create was a success
    */
  def save(thank: Thank): Future[Boolean]

  /**
    * Checks if user thanked resource
    */
  def thanked(giver: UserID, resource: Resource): Future[Option[Boolean]]

  /**
    * Find [[Thank]]
    *
    * @return existing or creates new [[Thank]]
    */
  def findByResource(uri: Resource): Future[Option[Thank]]

  /**
    * Increases number of thanks given
    *
    * @return true, if update passed
    */
  def increase(user: String, url: Resource): Future[Boolean]

  /**
    * Updates current resource ownership and all it's children
    */
  def updateOwner(owner: SupportedProject, url: Resource): Future[Boolean]

}
