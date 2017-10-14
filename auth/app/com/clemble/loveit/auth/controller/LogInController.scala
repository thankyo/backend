package com.clemble.loveit.auth.controller

import javax.inject.Inject

import com.clemble.loveit.auth.service.AuthService
import com.clemble.loveit.common.util.AuthEnv
import com.mohiva.play.silhouette.api._
import com.mohiva.play.silhouette.api.util.Credentials
import play.api.i18n.I18nSupport
import play.api.libs.json.{Json, OFormat}
import play.api.mvc._

import scala.concurrent.ExecutionContext

/**
  * The `Sign In` controller.
  *
  * @param components The Play controller components.
  * @param silhouette The Silhouette stack.
  */
class LogInController @Inject()(
                                 authService: AuthService,
                                 components: ControllerComponents
                               )(
                                 implicit
                                 silhouette: Silhouette[AuthEnv],
                                 ex: ExecutionContext
                               ) extends AbstractController(components) with I18nSupport {


  implicit val credentialsJson: OFormat[Credentials] = Json.format[Credentials]

  /**
    * Handles the submitted form.
    *
    * @return The result to display.
    */
  def submit: Action[Credentials] = silhouette.UnsecuredAction.async(parse.json[Credentials]) { implicit req: Request[Credentials] =>
    authService.
      login(req.body).
      flatMap(AuthUtils.authResponse).
      recover({ case t: Throwable => BadRequest(t.getMessage) })
  }

}