package com.clemble.thank.service.repository

import com.clemble.thank.model.error.RepositoryException
import com.clemble.thank.model.{Amount, User, UserId}

import scala.concurrent.Future

/**
  * [[User]] repository
  */
trait UserRepository {

  /**
    * Find [[User]] by ID
    *
    * @return
    */
  def findById(id: UserId): Future[Option[User]]

  /**
    * Create a new user in the system
    *
    * @param user user to create
    * @return saved user
    * @throws RepositoryException in case of duplicate code or any other problem
    */
  @throws[RepositoryException]
  def save(user: User): Future[User]

  /**
    * Change user balance
    *
    * @return
    */
  def changeBalance(id: UserId, diff: Amount): Future[Boolean]

}
