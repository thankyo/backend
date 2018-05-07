package com.clemble.loveit.auth.controller

import javax.inject.Inject

import com.clemble.loveit.auth.model.requests.RegistrationRequest
import com.clemble.loveit.auth.service.AuthService
import com.clemble.loveit.common.controller.{CookieUtils, LoveItController}
import com.clemble.loveit.common.util.AuthEnv
import com.mohiva.play.silhouette.api.Silhouette
import play.api.libs.json.JsBoolean
import play.api.mvc.{ControllerComponents, Request}

import scala.concurrent.ExecutionContext

class UnRegisterController @Inject()(
  authService: AuthService,
  components: ControllerComponents
)(
  implicit
  silhouette: Silhouette[AuthEnv],
  cookieUtils: CookieUtils,
  ex: ExecutionContext
) extends LoveItController(silhouette, components){

  def removeProvider(provider: String) = silhouette.SecuredAction.async{ req =>
    authService.removeSocial(req.identity.id, provider).map({
      case None => NotFound
      case Some(user) => Ok(user)
    })
  }

}
