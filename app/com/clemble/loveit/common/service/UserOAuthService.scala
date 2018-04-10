package com.clemble.loveit.common.service

import com.mohiva.play.silhouette.api.{AuthInfo, LoginInfo}

import scala.concurrent.Future

trait UserOAuthService {

  def findAuthInfo(loginInfo: LoginInfo): Future[Option[AuthInfo]]

}
