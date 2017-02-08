package com.clemble.thank.service

import com.clemble.thank.model.User

import scala.concurrent.Future


trait UserService {

  def create(user: User): Future[User]

}
