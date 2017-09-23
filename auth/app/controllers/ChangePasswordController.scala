package controllers

import javax.inject.Inject

import com.clemble.loveit.common.util.AuthEnv
import com.mohiva.play.silhouette.api._
import com.mohiva.play.silhouette.api.actions.SecuredRequest
import com.mohiva.play.silhouette.api.exceptions.ProviderException
import com.mohiva.play.silhouette.api.repositories.AuthInfoRepository
import com.mohiva.play.silhouette.api.util.{Credentials, PasswordHasherRegistry, PasswordInfo}
import com.mohiva.play.silhouette.impl.providers.CredentialsProvider
import forms.ChangePasswordForm
import org.webjars.play.WebJarsUtil
import play.api.i18n.{I18nSupport, Messages}
import play.api.mvc.{AbstractController, AnyContent, ControllerComponents}
import utils.auth.{WithProvider}

import scala.concurrent.{ExecutionContext, Future}

/**
 * The `Change Password` controller.
 *
 * @param components             The Play controller components.
 * @param silhouette             The Silhouette stack.
 * @param credentialsProvider    The credentials provider.
 * @param authInfoRepository     The auth info repository.
 * @param passwordHasherRegistry The password hasher registry.
 * @param webJarsUtil            The webjar util.
 * @param assets                 The Play assets finder.
 * @param ex                     The execution context.
 */
class ChangePasswordController @Inject() (
                                           components: ControllerComponents,
                                           silhouette: Silhouette[AuthEnv],
                                           credentialsProvider: CredentialsProvider,
                                           authInfoRepository: AuthInfoRepository,
                                           passwordHasherRegistry: PasswordHasherRegistry
)(
  implicit
  webJarsUtil: WebJarsUtil,
  assets: AssetsFinder,
  ex: ExecutionContext
) extends AbstractController(components) with I18nSupport {

  /**
   * Views the `Change Password` page.
   *
   * @return The result to display.
   */
  def view = silhouette.SecuredAction(WithProvider[AuthEnv#A](CredentialsProvider.ID)) {
    implicit request: SecuredRequest[AuthEnv, AnyContent] =>
      Ok(views.html.changePassword(ChangePasswordForm.form, request.identity))
  }

  /**
   * Changes the password.
   *
   * @return The result to display.
   */
  def submit = silhouette.SecuredAction(WithProvider[AuthEnv#A](CredentialsProvider.ID)).async {
    implicit request: SecuredRequest[AuthEnv, AnyContent] =>
      ChangePasswordForm.form.bindFromRequest.fold(
        form => Future.successful(BadRequest(views.html.changePassword(form, request.identity))),
        password => {
          val (currentPassword, newPassword) = password
          val credentials = Credentials(request.identity.email.getOrElse(""), currentPassword)
          credentialsProvider.authenticate(credentials).flatMap { loginInfo =>
            val passwordInfo = passwordHasherRegistry.current.hash(newPassword)
            authInfoRepository.update[PasswordInfo](loginInfo, passwordInfo).map { _ =>
              Redirect(routes.ChangePasswordController.view()).flashing("success" -> Messages("password.changed"))
            }
          }.recover {
            case _: ProviderException =>
              Redirect(routes.ChangePasswordController.view()).flashing("error" -> Messages("current.password.invalid"))
          }
        }
      )
  }
}
