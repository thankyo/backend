package com.clemble.loveit.auth.controller

import javax.inject.Inject

import com.clemble.loveit.common.util.AuthEnv
import com.mohiva.play.silhouette.api._
import com.mohiva.play.silhouette.api.repositories.AuthInfoRepository
import com.mohiva.play.silhouette.api.util.{Credentials, PasswordHasherRegistry, PasswordInfo}
import com.mohiva.play.silhouette.impl.providers.CredentialsProvider
import com.clemble.loveit.auth.model.requests.ChangePasswordRequest
import com.clemble.loveit.common.controller.LoveItController
import play.api.i18n.I18nSupport
import play.api.libs.json.JsBoolean
import play.api.mvc.{ControllerComponents}

import scala.concurrent.ExecutionContext

/**
  * The `Change Password` controller.
  *
  * @param components             The Play controller components.
  * @param silhouette             The Silhouette stack.
  * @param credentialsProvider    The credentials provider.
  * @param authInfoRepository     The auth info repository.
  * @param passwordHasherRegistry The password hasher registry.
  * @param ex                     The execution context.
  */
class ChangePasswordController @Inject()(
                                          components: ControllerComponents,
                                          silhouette: Silhouette[AuthEnv],
                                          credentialsProvider: CredentialsProvider,
                                          authInfoRepository: AuthInfoRepository,
                                          passwordHasherRegistry: PasswordHasherRegistry
                                        )(
                                          implicit
                                          ex: ExecutionContext
                                        ) extends LoveItController(components) with I18nSupport {

  /**
    * Changes the password.
    *
    * @return The result to display.
    */
  def submit = silhouette.SecuredAction.async(parse.json[ChangePasswordRequest])({
    request => {
      val (currentPassword, newPassword) = ChangePasswordRequest.unapply(request.body).get
      val credentials = Credentials(request.identity.email, currentPassword)
      val passwordInfo = passwordHasherRegistry.current.hash(newPassword)
      for {
        loginInfo <- credentialsProvider.authenticate(credentials)
        update <- authInfoRepository.update[PasswordInfo](loginInfo, passwordInfo)
      } yield {
        Ok(JsBoolean(true))
      }
    }
  })

}
