package com.clemble.loveit.common.service

import com.clemble.loveit.common.model.UserID
import com.mohiva.play.silhouette.api.{AuthInfo, LoginInfo}

import scala.concurrent.Future

trait UserOAuthService {

  def findAuthInfo(user: UserID, provider: String): Future[Option[AuthInfo]]

}
