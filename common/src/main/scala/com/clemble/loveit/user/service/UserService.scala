package com.clemble.loveit.user.service

import com.clemble.loveit.common.model.{Email, UserID}
import com.clemble.loveit.user.model.User
import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.api.services.IdentityService
import com.mohiva.play.silhouette.impl.providers.CommonSocialProfile

import scala.concurrent.Future

trait UserService extends IdentityService[User] {

  /**
    * Create a new user in the system
    *
    * @param user user to create
    * @return saved user
    */
  def save(user: User): Future[User]

  def retrieve(loginInfo: LoginInfo): Future[Option[User]]

  def findById(userId: UserID): Future[Option[User]]

  def findByEmail(email: Email): Future[Option[User]]

  /**
    *
    * @param profile Either Left if user already existed or Right if it's a new user
    */
  def createOrUpdateUser(profile: CommonSocialProfile): Future[Either[User, User]]

}
