package com.clemble.loveit.auth.controller

import java.util.UUID

import com.clemble.loveit.auth.model.requests.{ResetPasswordRequest, RestorePasswordRequest}
import com.clemble.loveit.auth.service.ResetPasswordService
import com.clemble.loveit.common.controller.{CookieUtils, LoveItController}
import com.clemble.loveit.common.service.UserService
import com.clemble.loveit.common.util.AuthEnv
import com.mohiva.play.silhouette.api._
import com.mohiva.play.silhouette.api.repositories.AuthInfoRepository
import com.mohiva.play.silhouette.api.util.PasswordHasherRegistry
import javax.inject.Inject
import play.api.i18n.I18nSupport
import play.api.libs.json.JsBoolean
import play.api.mvc._

import scala.concurrent.ExecutionContext

/**
  * The `Reset Password` controller.
  *
  * @param components             The Play controller components.
  * @param silhouette             The Silhouette stack.
  * @param resetPasswordService       The auth token service implementation.
  * @param ex                     The execution context.
  */
class ResetPasswordController @Inject()(
  components: ControllerComponents,
  resetPasswordService: ResetPasswordService
)(
  implicit
  silhouette: Silhouette[AuthEnv],
  cookieUtils: CookieUtils,
  ex: ExecutionContext
) extends LoveItController(silhouette, components) with I18nSupport {

  /**
    * Sends an email with password reset instructions.
    *
    * It sends an email to the given address if it exists in the database. Otherwise we do not show the user
    * a notice for not existing email addresses to prevent the leak of existing email addresses.
    *
    * @return The result to display.
    */
  def resetPassword = silhouette.UnsecuredAction.async(parse.json[ResetPasswordRequest])({ implicit request => {
    resetPasswordService.create(request.body).map(_ => Ok(JsBoolean(true)))
  }
  })


  /**
    * Resets the password.
    *
    * @param token The token to identify a user.
    * @return The result to display.
    */
  def restorePassword(token: UUID) = silhouette.UnsecuredAction.async(parse.json[RestorePasswordRequest])({ implicit request =>
    for {
      loggedIn <- resetPasswordService.restore(token, request.body)
      authResult <- AuthUtils.authResponse(loggedIn)
    } yield {
      authResult
    }
  })

}
