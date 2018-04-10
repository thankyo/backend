package com.clemble.loveit.common.model

import com.clemble.loveit.common.service.TumblrProvider
import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.impl.providers.CredentialsProvider
import com.mohiva.play.silhouette.impl.providers.oauth2.{FacebookProvider, GoogleProvider}

case class UserSocialConnections(
  credentials: Option[String] = None,
  facebook: Option[String] = None,
  google: Option[String] = None,
  tumblr: Option[String] = None
) {

  def asGoogleLogin(): Option[LoginInfo] = google.map(LoginInfo(GoogleProvider.ID, _))

  def asFacebookLogin(): Option[LoginInfo] = facebook.map(LoginInfo(FacebookProvider.ID, _))

  def asCredentialsLogin(): Option[LoginInfo] = credentials.map(LoginInfo(CredentialsProvider.ID, _))

  def asTumblrLogin(): Option[LoginInfo] = tumblr.map(LoginInfo(TumblrProvider.ID, _))

  def get(provider: String): Option[LoginInfo] = {
    provider match {
      case GoogleProvider.ID => asGoogleLogin()
      case FacebookProvider.ID => asFacebookLogin()
      case CredentialsProvider.ID => asCredentialsLogin()
      case TumblrProvider.ID => asTumblrLogin()
      case _ => None
    }
  }

  def add(loginInfo: LoginInfo): UserSocialConnections = {
    loginInfo.providerID match {
      case FacebookProvider.ID => copy(facebook = Some(loginInfo.providerKey))
      case GoogleProvider.ID => copy(google = Some(loginInfo.providerKey))
      case CredentialsProvider.ID => copy(credentials = Some(loginInfo.providerKey))
      case TumblrProvider.ID => copy(tumblr = Some(loginInfo.providerKey))
    }
  }

  def remove(provider: String): UserSocialConnections = {
    provider match {
      case GoogleProvider.ID => copy(google = None)
      case FacebookProvider.ID => copy(facebook = None)
      case CredentialsProvider.ID => copy(credentials = None)
      case TumblrProvider.ID => copy(tumblr = None)
      case _ => this
    }
  }

}
