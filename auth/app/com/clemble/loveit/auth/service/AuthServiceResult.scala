package com.clemble.loveit.auth.service

import com.clemble.loveit.user.model.User
import com.mohiva.play.silhouette.api.LoginInfo

sealed trait AuthServiceResult {
  val user: User
  val loginInfo: LoginInfo
}

case class UserRegister(user: User, loginInfo: LoginInfo) extends AuthServiceResult

case class UserLoggedIn(user: User, loginInfo: LoginInfo) extends AuthServiceResult