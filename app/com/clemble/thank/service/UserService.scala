package com.clemble.thank.service

import com.clemble.thank.model.{Amount, ResourceOwnership, User, UserId}

import scala.concurrent.Future

trait UserService {

  def assignOwnership(userId: UserId, ownership: ResourceOwnership): Future[ResourceOwnership]

  def updateOwnerBalance(url: String, change: Amount): Future[Boolean]

  def updateBalance(user: UserId, change: Amount): Future[Boolean]

}
