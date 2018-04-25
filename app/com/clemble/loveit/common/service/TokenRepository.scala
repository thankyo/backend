package com.clemble.loveit.common.service

import java.util.UUID

import com.clemble.loveit.common.model.{Token, UserID}

import scala.concurrent.Future

trait TokenRepository[T <: Token] {

  def findAndRemoveByToken(token: UUID): Future[Option[T]]

  def save(token: T): Future[T]

  def removeByToken(token: UUID): Future[Boolean]

  def removeByUser(user: UserID): Future[Boolean]

}
