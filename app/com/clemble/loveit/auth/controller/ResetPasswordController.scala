package com.clemble.loveit.auth.controller

import java.util.UUID

import javax.inject.Inject
import com.clemble.loveit.auth.model.requests.RestorePasswordRequest
import com.clemble.loveit.auth.service.{ResetPasswordTokenService, UserLoggedIn}
import com.clemble.loveit.common.controller.{CookieUtils, LoveItController}
import com.clemble.loveit.common.error.FieldValidationError
import com.clemble.loveit.common.service.UserService
import com.clemble.loveit.common.util.AuthEnv
import com.mohiva.play.silhouette.api._
import com.mohiva.play.silhouette.api.repositories.AuthInfoRepository
import com.mohiva.play.silhouette.api.util.{PasswordHasherRegistry, PasswordInfo}
import com.mohiva.play.silhouette.impl.providers.CredentialsProvider
import play.api.i18n.I18nSupport
import play.api.mvc._

import scala.concurrent.ExecutionContext

/**
  * The `Reset Password` controller.
  *
  * @param components             The Play controller components.
  * @param silhouette             The Silhouette stack.
  * @param userService            The user service implementation.
  * @param authInfoRepository     The auth info repository.
  * @param passwordHasherRegistry The password hasher registry.
  * @param authTokenService       The auth token service implementation.
  * @param ex                     The execution context.
  */
class ResetPasswordController @Inject()(
                                         components: ControllerComponents,
                                         userService: UserService,
                                         authInfoRepository: AuthInfoRepository,
                                         passwordHasherRegistry: PasswordHasherRegistry,
                                         authTokenService: ResetPasswordTokenService
                                       )(
                                         implicit
                                         silhouette: Silhouette[AuthEnv],
                                         cookieUtils: CookieUtils,
                                         ex: ExecutionContext
                                       ) extends LoveItController(components) with I18nSupport {

  /**
    * Resets the password.
    *
    * @param token The token to identify a user.
    * @return The result to display.
    */
  def submit(token: UUID) = silhouette.UnsecuredAction.async(parse.json[RestorePasswordRequest]) { implicit request =>
    request.body.validate()

    val passwordInfo = passwordHasherRegistry.current.hash(request.body.password)
    for {
      authTokenOpt <- authTokenService.validate(token)
      authToken = authTokenOpt.getOrElse({
        throw FieldValidationError("password", "Token expired or already used")
      })
      userOpt <- userService.findById(authToken.user)
      loginInfoOpt = userOpt.flatMap(_.profiles.credentials.map(providerKey => LoginInfo(CredentialsProvider.ID, providerKey)))
      _ <- authInfoRepository.update[PasswordInfo](loginInfoOpt.get, passwordInfo)
      authResult <- AuthUtils.authResponse(UserLoggedIn(userOpt.get, loginInfoOpt.get))
    } yield {
      authResult
    }
  }

}
