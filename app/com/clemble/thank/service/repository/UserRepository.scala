package com.clemble.thank.service.repository

import com.clemble.thank.model.error.RepositoryException
import com.clemble.thank.model.{User, UserId}
import play.api.libs.iteratee.Enumerator

import scala.concurrent.Future

/**
  * [[User]] repository
  */
trait UserRepository {

  def findAll(): Enumerator[User]

  def findById(id: UserId): Future[Option[User]]

  /**
    * Create a new user in the system
    *
    * @param user user to create
    * @return saved user
    * @throws RepositoryException in case of duplicate code or any other problem
    */
  @throws[RepositoryException]
  def create(user: User): Future[User]

}
