package com.clemble.loveit.common.service

import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.impl.providers.OAuth2Info

import scala.concurrent.Future

trait UserOAuthService {

  def findAuthInfo(loginInfo: LoginInfo): Future[Option[OAuth2Info]]

}
