package com.clemble.loveit.auth.controller

import com.clemble.loveit.auth.service.{AuthServiceResult, UserLoggedIn, UserRegister}
import com.clemble.loveit.common.controller.CookieUtils
import com.clemble.loveit.common.util.AuthEnv
import com.clemble.loveit.user.model.User
import com.mohiva.play.silhouette.api.{LoginEvent, LoginInfo, SignUpEvent, Silhouette}
import play.api.libs.json.JsString
import play.api.mvc.{RequestHeader, Result, Results}

import scala.concurrent.{ExecutionContext, Future}

object AuthUtils {

  def authResponse(authRes: AuthServiceResult)(implicit req: RequestHeader, ec: ExecutionContext, silhouette: Silhouette[AuthEnv]): Future[Result] = {
    val user = authRes.user
    val userDetails = Some(User.jsonFormat.writes(user))
    for {
      authenticator <- silhouette.env.authenticatorService.create(authRes.loginInfo)
      authenticatorWithClaim = authenticator.copy(customClaims = userDetails)
      value <- silhouette.env.authenticatorService.init(authenticatorWithClaim)
      httpRes <- silhouette.env.authenticatorService.embed(value, Results.Ok(JsString(value)))
    } yield {
      authRes match {
        case _: UserRegister =>
          silhouette.env.eventBus.publish(SignUpEvent(user, req))
          silhouette.env.eventBus.publish(LoginEvent(user, req))
        case _: UserLoggedIn =>
          silhouette.env.eventBus.publish(LoginEvent(user, req))
      }

      CookieUtils.setUser(httpRes, user.id)
    }
  }

}
