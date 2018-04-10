package com.clemble.loveit.auth.controller

import com.clemble.loveit.auth.model.AuthResponse
import com.clemble.loveit.auth.service.{AuthServiceResult, UserRegister}
import com.clemble.loveit.common.controller.CookieUtils
import com.clemble.loveit.common.util.AuthEnv
import com.clemble.loveit.common.model.User
import com.mohiva.play.silhouette.api.{LoginEvent, SignUpEvent, Silhouette}
import play.api.mvc.{RequestHeader, Result, Results}

import scala.concurrent.{ExecutionContext, Future}

object AuthUtils {

  def authResponse(authRes: AuthServiceResult)(implicit req: RequestHeader, ec: ExecutionContext, silhouette: Silhouette[AuthEnv], cookieUtils: CookieUtils): Future[Result] = {
    val user = authRes.user
    val userDetails = Some(User.jsonFormat.writes(user))
    val existingUser = authRes match {
      case _: UserRegister => false
      case _ => true
    }
    for {
      authenticator <- silhouette.env.authenticatorService.create(authRes.loginInfo)
      authenticatorWithClaim = authenticator.copy(customClaims = userDetails)
      token <- silhouette.env.authenticatorService.init(authenticatorWithClaim)
      httpRes <- silhouette.env.authenticatorService.embed(token, Results.Ok(AuthResponse(token, existingUser)))
    } yield {
      if (!existingUser) {
        silhouette.env.eventBus.publish(SignUpEvent(user, req))
      }
      silhouette.env.eventBus.publish(LoginEvent(user, req))

      cookieUtils.setUser(httpRes, user.id)
    }
  }

}
