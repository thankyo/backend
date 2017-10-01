package com.clemble.loveit.thank.service.repository

import com.clemble.loveit.common.model.{Resource, UserID}

import scala.concurrent.Future

/**
  * Resource ownership repository
  */
trait ResourceOwnershipRepository {

  /**
    * Find possible resource owner
    */
  def findOwner(resource: Resource): Future[Option[UserID]]

  /**
    * Returns a set of resources owned, by the user
    * @return resources, belonging to this user
    */
  def listOwned(user: UserID): Future[Set[Resource]]

  /**
    * Assigns ownership of the resource
    * @return true is assignment was success
    */
  def assignOwnership(user: UserID, resource: Resource): Future[Boolean]

}
