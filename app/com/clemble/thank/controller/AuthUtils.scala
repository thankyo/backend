package com.clemble.thank.controller

import com.clemble.thank.model.UserId
import com.clemble.thank.util.AuthEnv
import com.mohiva.play.silhouette.api.actions.{SecuredRequest, UserAwareRequest}

object AuthUtils {

  def whoAmI[B](id: UserId)(implicit req: SecuredRequest[AuthEnv, B]) = {
    if (id == "me")
      req.identity.id
    else
      id
  }

}
