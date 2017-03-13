package com.clemble.thank.controller

import com.clemble.thank.model.UserId
import com.clemble.thank.util.AuthEnv
import com.mohiva.play.silhouette.api.actions.UserAwareRequest

object AuthUtils {

  def whoAmI[B](id: UserId)(implicit req: UserAwareRequest[AuthEnv, B]) = {
    if (id == "me")
      req.identity.map(_.id).getOrElse(id)
    else
      id
  }

}
