package com.clemble.thank.repository

import com.clemble.thank.model.User
import play.api.libs.iteratee.Enumerator

import scala.concurrent.Future

/**
  * [[User]] repository
  */
trait UserRepository {

  def findAll(): Enumerator[User]

  def findByNickname(nickname: String): Future[Option[User]]

}
