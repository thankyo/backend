package com.clemble.thank.services

import com.clemble.thank.model.User

import scala.concurrent.Future


trait UserService {

  def create(user: User): Future[User]

}
