package com.clemble.loveit.common.service

import com.clemble.loveit.common.model.{Email, User, UserID}
import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.api.services.IdentityService

import scala.concurrent.Future

trait UserService extends IdentityService[User] {

  def create(user: User): Future[User]

  def retrieve(loginInfo: LoginInfo): Future[Option[User]]

  def findById(userId: UserID): Future[Option[User]]

  def findByEmail(email: Email): Future[Option[User]]

  def update(user: User): Future[User]

}
