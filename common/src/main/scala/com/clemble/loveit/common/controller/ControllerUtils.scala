package com.clemble.loveit.common.controller

import com.clemble.loveit.common.model.UserID
import com.clemble.loveit.common.util.AuthEnv
import com.mohiva.play.silhouette.api.actions.SecuredRequest

object ControllerUtils {

  def idOrMe(id: UserID)(implicit req: SecuredRequest[AuthEnv, _]): UserID = {
    if (id == "my") {
      req.identity.id
    } else {
      id
    }
  }

}
