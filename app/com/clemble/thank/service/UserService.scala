package com.clemble.thank.service

import com.clemble.thank.model.{Amount, User, UserId}

import scala.concurrent.Future

trait UserService {

  def create(user: User): Future[User]

  def get(user: UserId): Future[Option[User]]

  def updateOwnerBalance(url: String, change: Amount): Future[Boolean]

  def updateBalance(user: UserId, change: Amount): Future[Boolean]

}
