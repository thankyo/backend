package com.clemble.loveit.common.controller

import java.util.concurrent.TimeUnit

import com.clemble.loveit.common.model.UserID
import play.api.mvc.{Cookie, Request, Result}

object CookieUtils {

  private val COOKIE_NAME = "userID"
  private val COOKIE_MAX_AGE = Some(TimeUnit.DAYS.toSeconds(90).toInt)

  def readUser[A](req: Request[A]): Option[UserID] = {
    req.cookies.find(_.name == COOKIE_NAME).map(_.value)
  }

  def setUser(res: Result, userID: UserID): Result = {
    res.withCookies(Cookie(COOKIE_NAME, userID, maxAge = COOKIE_MAX_AGE))
  }

  def removeUser(): Cookie = {
    Cookie(COOKIE_NAME, "bue", maxAge = Some(0))
  }

}
