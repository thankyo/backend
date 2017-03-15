package com.clemble.thank.service

import com.clemble.thank.model._

import scala.concurrent.Future

trait UserService {

  def findById(userId: UserID): Future[Option[User]]

  def assignOwnership(userId: UserID, ownership: ResourceOwnership): Future[ResourceOwnership]

  def findResourceOwner(uri: Resource): Future[User]

  def updateBalance(user: UserID, change: Amount): Future[Boolean]

}
