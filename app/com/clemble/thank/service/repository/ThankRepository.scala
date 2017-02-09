package com.clemble.thank.service.repository

import com.clemble.thank.model.Thank
import play.api.libs.iteratee.Enumerator

import scala.concurrent.Future

/**
  * [[Thank]] repository
  */
trait ThankRepository {

  /**
    * Create appropriate url
    * @param thank object to create
    * @return persisted Thank
    */
  def create(thank: Thank): Future[Thank]

  /**
    * Find [[Thank]]
    *
    * @return existing or creates new [[Thank]]
    */
  def findByUrl(url: String): Future[Option[Thank]]

  /**
    * Increases number of thanks given
    *
    * @return updated [[Thank]]
    */
  def increase(url: String): Future[Thank]

  /**
    * Finds all thanks which this Thank is a root of
    *
    * @return [[Enumerator]] of [[Thank]]
    */
  def findAllIncludingSub(url: String): Enumerator[Thank]

}
