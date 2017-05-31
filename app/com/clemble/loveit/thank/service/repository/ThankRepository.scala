package com.clemble.loveit.thank.service.repository

import com.clemble.loveit.common.model.Resource
import com.clemble.loveit.thank.model.Thank

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
    * Decrease number of thanks given
    * @return true, if update passed
    */
  def decrease(user: String, url: Resource): Future[Boolean]

}
