package com.clemble.loveit.auth.controller

import com.clemble.loveit.auth.model.requests.ForgotPasswordRequest
import com.clemble.loveit.auth.service.{EmailService, ResetPasswordTokenService}
import com.clemble.loveit.auth.views.html.emails.resetPassword
import com.clemble.loveit.auth.views.txt.emails
import com.clemble.loveit.common.controller.LoveItController
import com.clemble.loveit.common.error.FieldValidationError
import com.clemble.loveit.common.service.UserService
import com.clemble.loveit.common.util.AuthEnv
import com.mohiva.play.silhouette.api._
import com.mohiva.play.silhouette.impl.exceptions.IdentityNotFoundException
import javax.inject.Inject
import org.matthicks.mailgun.{EmailAddress, Message}
import play.api.i18n.{I18nSupport, Messages}
import play.api.libs.json.JsBoolean
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}

/**
  * The `Forgot Password` controller.
  *
  * @param components       The Play controller components.
  * @param silhouette       The Silhouette stack.
  * @param userService      The user service implementation.
  * @param authTokenService The auth token service implementation.
  * @param ex               The execution context.
  */
class ForgotPasswordController @Inject()(
  components: ControllerComponents,
  silhouette: Silhouette[AuthEnv],
  userService: UserService,
  authTokenService: ResetPasswordTokenService,
  emailService: EmailService
)(
  implicit
  ex: ExecutionContext
) extends LoveItController(components) with I18nSupport {

  /**
    * Sends an email with password reset instructions.
    *
    * It sends an email to the given address if it exists in the database. Otherwise we do not show the user
    * a notice for not existing email addresses to prevent the leak of existing email addresses.
    *
    * @return The result to display.
    */
  def submit = silhouette.UnsecuredAction.async(parse.json[ForgotPasswordRequest])({
    implicit request => {
      val loginInfo = request.body.toLoginInfo

      for {
        user <- userService.retrieve(loginInfo).flatMap({
          case Some(user) => Future.successful(user)
          case None => userService.
            findByEmail(request.body.email).
            map({
              case Some(user) =>
                import user.profiles._
                val regStr = List(facebook.map(_ => "FB"), google.map(_ => "Google"), credentials.map(_ => "Credentials")).flatten.mkString(", ")
                throw FieldValidationError("email", s"You are registered through ${regStr}")
              case None =>
                throw new IdentityNotFoundException(s"No user with ${loginInfo.providerKey}")
            })
        })
        authToken <- authTokenService.create(user.id)
        emailSent <- emailService.sendResetPasswordEmail(user, authToken)
      } yield {
        Ok(JsBoolean(emailSent))
      }
    }
  })

}
