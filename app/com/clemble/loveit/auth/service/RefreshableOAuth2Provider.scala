package com.clemble.loveit.auth.service

import com.mohiva.play.silhouette.impl.providers.{OAuth2Info}
import com.mohiva.play.silhouette.impl.providers.oauth2.{BaseGoogleProvider}

import scala.concurrent.Future

trait RefreshableOAuth2Provider {

  def refresh(refreshToken: String): Future[OAuth2Info]

}

trait GoogleRefreshableProvider extends RefreshableOAuth2Provider with BaseGoogleProvider {

  override def refresh(refreshToken: String): Future[OAuth2Info] = {
    val params = Map(
      "client_id" -> settings.clientID,
      "client_secret" -> settings.clientSecret,
      "refresh_token" -> refreshToken,
      "grant_type" -> "refresh_token"
    )
    httpLayer.url("https://www.googleapis.com/oauth2/v4/token").post(params).flatMap(response => Future.fromTry(buildInfo(response)))
  }

}

