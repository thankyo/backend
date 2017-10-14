package com.clemble.loveit.auth.controller

import javax.inject.Inject

import com.clemble.loveit.common.util.AuthEnv
import com.mohiva.play.silhouette.api._
import com.clemble.loveit.auth.model.requests.RegisterRequest
import com.clemble.loveit.auth.service.{AuthService}
import play.api.i18n.I18nSupport
import play.api.mvc._

import scala.concurrent.{ExecutionContext}

/**
  * The `Register` controller.
  *
  * @param components             The Play controller components.
  * @param silhouette             The Silhouette stack.
  * @param ex                     The execution context.
  */
class RegisterController @Inject()(
                                  authService: AuthService,
                                  components: ControllerComponents
                                )(
                                  implicit
                                  silhouette: Silhouette[AuthEnv],
                                  ex: ExecutionContext
                                ) extends AbstractController(components) with I18nSupport {

  /**
    * Handles the submitted form.
    *
    * @return The result to display.
    */
  def submit: Action[RegisterRequest] = silhouette.UnsecuredAction.async(parse.json[RegisterRequest]) { implicit req: Request[RegisterRequest] =>
    authService.
      register(req.body).
      flatMap(AuthUtils.authResponse).
      recover({
        case t: Throwable => BadRequest(t.getMessage)
      })
  }
}
