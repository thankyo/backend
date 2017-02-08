package com.clemble.thank.service

import com.clemble.thank.model.{User, UserId}

import scala.concurrent.Future


trait UserService {

  def create(user: User): Future[User]

  def get(user: UserId): Future[Option[User]]

}
