package com.clemble.thank.service

import com.clemble.thank.model._

import scala.concurrent.Future

trait UserService {

  def findById(userId: UserId): Future[Option[User]]

  def assignOwnership(userId: UserId, ownership: ResourceOwnership): Future[ResourceOwnership]

  def findResourceOwner(uri: URI): Future[User]

  def updateBalance(user: UserId, change: Amount): Future[Boolean]

}
