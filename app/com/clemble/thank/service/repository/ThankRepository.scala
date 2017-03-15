package com.clemble.thank.service.repository

import com.clemble.thank.model.{Resource, Thank}

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
  def increase(url: Resource): Future[Boolean]

}
