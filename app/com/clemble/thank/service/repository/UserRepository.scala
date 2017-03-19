package com.clemble.thank.service.repository

import com.clemble.thank.model.error.RepositoryException
import com.clemble.thank.model._
import com.clemble.thank.payment.model.BankDetails
import com.mohiva.play.silhouette.api.services.IdentityService

import scala.concurrent.Future

/**
  * [[User]] repository
  */
trait UserRepository extends IdentityService[UserIdentity] {

  /**
    * Find [[User]] by ID
    */
  def findById(id: UserID): Future[Option[User]]

  /**
    * Find owner of provided uri
    */
  def findOwners(uris: List[ResourceOwnership]): Future[List[User]]

  /**
    * Looks for all related Resources based on the provided URL
    */
  def findRelated(uri: ResourceOwnership): Future[List[User]]

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
    * Remove users
    */
  def remove(users: Seq[UserID]): Future[Boolean]

  /**
    * Adds bank details to provided user
    */
  def setBankDetails(user: UserID, bankDetails: BankDetails): Future[Boolean]

  /**
    * Change user balance
    *
    * @return
    */
  def changeBalance(id: UserID, diff: Amount): Future[Boolean]

}
