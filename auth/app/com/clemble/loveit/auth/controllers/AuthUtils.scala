package com.clemble.loveit.auth.controllers

import com.clemble.loveit.common.controller.CookieUtils
import com.clemble.loveit.common.util.AuthEnv
import com.clemble.loveit.user.model.User
import com.mohiva.play.silhouette.api.{LoginInfo, Silhouette}
import play.api.libs.json.{JsObject, JsString, Json}
import play.api.mvc.{RequestHeader, Result, Results}

import scala.concurrent.{ExecutionContext, Future}

object AuthUtils {

  def authResponse(user: User, loginInfo: LoginInfo)(implicit req: RequestHeader, ec: ExecutionContext, silhouette: Silhouette[AuthEnv]): Future[Result] = {
    val userDetails = Some(Json.toJson(user).as[JsObject])
    for {
      authenticator <- silhouette.env.authenticatorService.create(loginInfo)
      authenticatorWithClaim = authenticator.copy(customClaims = userDetails)
      value <- silhouette.env.authenticatorService.init(authenticatorWithClaim)
      result <- silhouette.env.authenticatorService.embed(value, Results.Ok(JsString(value)))
    } yield {
      CookieUtils.setUser(result, user.id)
    }
  }

}
