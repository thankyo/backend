package com.clemble.loveit.user.service

import com.clemble.loveit.user.model._
import com.mohiva.play.silhouette.impl.providers.CommonSocialProfile

import com.clemble.loveit.common.model.{UserID}
import scala.concurrent.Future

trait UserService {

  def findById(userId: UserID): Future[Option[User]]

  /**
    *
    * @param profile Either Left if user already existed or Right if it's a new user
    */
  def createOrUpdateUser(profile: CommonSocialProfile): Future[Either[UserIdentity, UserIdentity]]

}
