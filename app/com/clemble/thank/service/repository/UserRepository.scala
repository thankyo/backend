package com.clemble.thank.service.repository

import com.clemble.thank.model.{User, UserId}
import play.api.libs.iteratee.Enumerator

import scala.concurrent.Future

/**
  * [[User]] repository
  */
trait UserRepository {

  def findAll(): Enumerator[User]

  def findById(id: UserId): Future[Option[User]]

  def create(user: User): Future[User]

}
