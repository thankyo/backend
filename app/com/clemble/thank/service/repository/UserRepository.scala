package com.clemble.thank.service.repository

import com.clemble.thank.model.error.RepositoryException
import com.clemble.thank.model.{Amount, ResourceOwnership, User, UserId}
import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.api.services.IdentityService

import scala.concurrent.Future

/**
  * [[User]] repository
  */
trait UserRepository extends IdentityService[User] {

  /**
    * Find [[User]] by ID
    */
  def findById(id: UserId): Future[Option[User]]

  /**
    * Find owner of provided uri
    */
  def findOwners(uris: List[ResourceOwnership]): Future[List[User]]

  /**
    * Finds user linked to provided profile
    */
  def retrieve(loginInfo: LoginInfo): Future[Option[User]]

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
    * Update existing user
    */
  def update(user: User): Future[User]

  /**
    * Change user balance
    *
    * @return
    */
  def changeBalance(id: UserId, diff: Amount): Future[Boolean]

}
