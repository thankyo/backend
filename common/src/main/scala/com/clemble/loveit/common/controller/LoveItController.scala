package com.clemble.loveit.common.controller

import com.clemble.loveit.common.model.UserID
import com.clemble.loveit.common.util.AuthEnv
import com.mohiva.play.silhouette.api.actions.SecuredRequest
import org.slf4j.LoggerFactory
import play.api.mvc.{AbstractController, ControllerComponents}

abstract class LoveItController(controllerComponents: ControllerComponents) extends AbstractController(controllerComponents) {

  val LOG = LoggerFactory.getLogger(getClass())

  val MY = "my"

  def idOrMe(id: UserID)(implicit req: SecuredRequest[AuthEnv, _]): UserID = {
    if (id == MY) {
      req.identity.id
    } else {
      id
    }
  }

  def isMe(id: UserID)(implicit req: SecuredRequest[AuthEnv, _]): Boolean = {
    id == MY || req.identity.id == id
  }

}
