package com.clemble.thank.services.repository

import com.clemble.thank.model.Thank
import play.api.libs.iteratee.Enumerator

import scala.concurrent.Future

/**
  * [[Thank]] repository
  */
trait ThankRepository {

  /**
    * Find [[Thank]]
    *
    * @return existing or creates new [[Thank]]
    */
  def findOrCreate(url: String): Future[Thank]

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
