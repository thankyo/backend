package controllers

import javax.inject.Inject

import com.clemble.loveit.common.util.AuthEnv
import com.clemble.loveit.user.service.UserService
import com.mohiva.play.silhouette.api._
import forms.ForgotPasswordRequest
import models.services.AuthTokenService
import play.api.i18n.{I18nSupport, Messages}
import play.api.libs.json.JsBoolean
import play.api.libs.mailer.{Email, MailerClient}
import play.api.mvc._

import scala.concurrent.ExecutionContext

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
                                          mailerClient: MailerClient
                                        )(
                                          implicit
                                          parser: PlayBodyParsers,
                                          ex: ExecutionContext
                                        ) extends AbstractController(components) with I18nSupport {

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
      val loginInfo = request.body.toLoginInfo()

      for {
        userOpt <- userService.retrieve(loginInfo) if (userOpt.isDefined)
        user = userOpt.get
        authToken <- authTokenService.create(user.id)
      } yield {
        val url = s"https://loveit.tips/password/${authToken.id}"
        mailerClient.send(
          Email(
            subject = Messages("email.reset.password.subject"),
            from = Messages("email.from"),
            to = Seq(loginInfo.providerKey),
            bodyText = Some(views.txt.emails.resetPassword(user, url).body),
            bodyHtml = Some(views.html.emails.resetPassword(user, url).body)
          ))
        Ok(JsBoolean(true))
      }
    }
  })

}
