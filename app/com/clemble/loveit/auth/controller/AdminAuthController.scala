package com.clemble.loveit.auth.controller

import com.clemble.loveit.auth.AdminAuthEnv
import com.clemble.loveit.common.controller.{AdminLoveItController}
import com.clemble.loveit.common.service.AuthyTwoFactoryProvider
import com.mohiva.play.silhouette.api.{LoginInfo, Silhouette}
import javax.inject.{Inject, Singleton}
import play.api.libs.json.{JsObject}
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}

@Singleton
case class AdminAuthController @Inject()(
  provider: AuthyTwoFactoryProvider,
  silhouette: Silhouette[AdminAuthEnv],
  components: ControllerComponents,
  implicit val ec: ExecutionContext
) extends AdminLoveItController(silhouette, components) {

  def adminAuthResponse(loginInfo: LoginInfo)(implicit req: RequestHeader): Future[Result] = {
    for {
      authenticator <- silhouette.env.authenticatorService.create(loginInfo)
      token <- silhouette.env.authenticatorService.init(authenticator)
      httpRes <- silhouette.env.authenticatorService.embed(token, Results.Ok("Success"))
    } yield {
      httpRes
    }
  }


  def authenticate() = silhouette.UnsecuredAction.async(parse.json[JsObject].map(_ \ "token").map(_.as[String]))(implicit req => {
    val loginInfo = LoginInfo(AuthyTwoFactoryProvider.ID, "admin")
    provider.authenticate(loginInfo, req.body).flatMap({
      case true => adminAuthResponse(loginInfo)
      case false => Future.successful(BadRequest("Try again"))
    })
  })

}
