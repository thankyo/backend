package com.clemble.loveit.auth.controller

import javax.inject.Inject

import com.clemble.loveit.common.util.AuthEnv
import com.clemble.loveit.user.service.UserService
import com.mohiva.play.silhouette.api._
import com.clemble.loveit.auth.model.requests.ForgotPasswordRequest
import com.clemble.loveit.auth.service.AuthTokenService
import com.clemble.loveit.auth.views.html.emails.resetPassword
import com.clemble.loveit.auth.views.txt.emails
import com.clemble.loveit.common.controller.LoveItController
import com.clemble.loveit.common.error.FieldValidationError
import com.mohiva.play.silhouette.impl.exceptions.IdentityNotFoundException
import org.matthicks.mailgun.{EmailAddress, Mailgun, Message}
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
  * @param mailerClient     The mailer client.
  * @param ex               The execution context.
  */
class ForgotPasswordController @Inject()(
                                          components: ControllerComponents,
                                          silhouette: Silhouette[AuthEnv],
                                          userService: UserService,
                                          authTokenService: AuthTokenService,
                                          mailerClient: Mailgun
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
        user <- userService.retrieve(loginInfo).flatMap(_ match {
          case Some(user) => Future.successful(user)
          case None => userService.
            findByEmail(request.body.email).
            map(_ match {
              case Some(user) =>
                throw FieldValidationError("email", s"You are registered through ${user.profiles.map(_.providerID).mkString(",")}")
              case None =>
                throw new IdentityNotFoundException(s"No user with ${loginInfo.providerKey}")
            })
        })
        authToken <- authTokenService.create(user.id)
      } yield {
        val url = s"https://loveit.tips/auth/reset/${authToken.token}"
        mailerClient.send(
          Message.simple(
            subject = Messages("email.reset.password.subject"),
            from = EmailAddress(Messages("email.from"), "Love it"),
            to = EmailAddress(loginInfo.providerKey),
            text = emails.resetPassword(user, url).body,
            html = resetPassword(user, url).body
          )
        )
        Ok(JsBoolean(true))
      }
    }
  })

}
