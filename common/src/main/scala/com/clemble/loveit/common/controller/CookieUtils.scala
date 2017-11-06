package com.clemble.loveit.common.controller

import java.util.concurrent.TimeUnit
import javax.inject.{Inject, Named, Singleton}

import com.clemble.loveit.common.model.UserID
import com.mohiva.play.silhouette.api.crypto.{Base64, Crypter}
import play.api.mvc.{Cookie, Request, Result}

@Singleton
case class CookieUtils @Inject() (@Named("cookieCrypter") crypter: Crypter) {

  private val COOKIE_NAME = "userID"
  private val COOKIE_MAX_AGE = Some(TimeUnit.DAYS.toSeconds(90).toInt)

  def readUser[A](req: Request[A]): Option[UserID] = {
    req.cookies.find(_.name == COOKIE_NAME).
      map(_.value).
      map(Base64.decode(_)).
      map(crypter.decrypt)
  }

  def setUser(res: Result, userID: UserID): Result = {
    res.withCookies(Cookie(COOKIE_NAME, Base64.encode(crypter.encrypt(userID)), maxAge = COOKIE_MAX_AGE))
  }

  def removeUser(): Cookie = {
    Cookie(COOKIE_NAME, "bye", maxAge = Some(0))
  }

}
