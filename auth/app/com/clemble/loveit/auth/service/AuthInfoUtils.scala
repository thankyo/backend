package com.clemble.loveit.auth.service

import java.time.{LocalDateTime}

import com.mohiva.play.silhouette.api.AuthInfo
import com.mohiva.play.silhouette.impl.providers.OAuth2Info

object AuthInfoUtils {

  def normalize[T <: AuthInfo](authInfo: T): T = {
    authInfo match {
      case oauthInfo: OAuth2Info if oauthInfo.expiresIn.isDefined =>
        val expireTime = LocalDateTime.now().plusSeconds(oauthInfo.expiresIn.get)
        val paramsWithCD = oauthInfo.params.getOrElse(Map.empty) + ("expires" -> expireTime.toString)
        oauthInfo.copy(params = Some(paramsWithCD)).asInstanceOf[T]
      case info => info
    }
  }

  def hasExpired[T <: AuthInfo](authInfo: T): Boolean = {
    authInfo match {
      case oauthInfo: OAuth2Info =>
        val expires = oauthInfo.params
          .flatMap(_.get("expires"))
          .map(dateStr => LocalDateTime.parse(dateStr))
        expires.exists(_.isBefore(LocalDateTime.now()))
      case _ => false
    }
  }

}
