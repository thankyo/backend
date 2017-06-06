package com.clemble.loveit.common.controller

import com.clemble.loveit.common.model.UserID
import play.api.mvc.{Request, Result}

object CookieUtils {

  def readUser[A](req: Request[A]): Option[UserID] = {
    None
  }

  def setUser(res: Result, userID: UserID): Result = {
    res
  }

}
