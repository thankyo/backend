package com.clemble.loveit.auth.controller

import com.clemble.loveit.common.controller.CookieUtils
import com.clemble.loveit.common.util.AuthEnv
import com.clemble.loveit.user.model.User
import com.mohiva.play.silhouette.api.{LoginEvent, LoginInfo, SignUpEvent, Silhouette}
import play.api.libs.json.{JsString}
import play.api.mvc.{RequestHeader, Result, Results}

import scala.concurrent.{ExecutionContext, Future}

object AuthUtils {

  def authResponse(eitherUser: Either[User, User], loginInfo: LoginInfo)(implicit req: RequestHeader, ec: ExecutionContext, silhouette: Silhouette[AuthEnv]): Future[Result] = {
    val user = eitherUser match {
      case Left(user) => user
      case Right(user) => user
    }
    val userDetails = Some(User.jsonFormat.writes(user))
    for {
      authenticator <- silhouette.env.authenticatorService.create(loginInfo)
      authenticatorWithClaim = authenticator.copy(customClaims = userDetails)
      value <- silhouette.env.authenticatorService.init(authenticatorWithClaim)
      result <- silhouette.env.authenticatorService.embed(value, Results.Ok(JsString(value)))
    } yield {
      if (eitherUser.isRight) {
        silhouette.env.eventBus.publish(SignUpEvent(user, req))
      }
      silhouette.env.eventBus.publish(LoginEvent(user, req))

      CookieUtils.setUser(result, user.id)
    }
  }

}
