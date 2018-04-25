package com.clemble.loveit.auth.controller

import java.util.UUID

import com.clemble.loveit.auth.model.requests.{ResetPasswordRequest, RestorePasswordRequest}
import com.clemble.loveit.auth.service.{EmailService, ResetPasswordTokenService, UserLoggedIn}
import com.clemble.loveit.common.controller.{CookieUtils, LoveItController}
import com.clemble.loveit.common.error.FieldValidationError
import com.clemble.loveit.common.service.UserService
import com.clemble.loveit.common.util.AuthEnv
import com.mohiva.play.silhouette.api._
import com.mohiva.play.silhouette.api.repositories.AuthInfoRepository
import com.mohiva.play.silhouette.api.util.{PasswordHasherRegistry, PasswordInfo}
import com.mohiva.play.silhouette.impl.exceptions.IdentityNotFoundException
import com.mohiva.play.silhouette.impl.providers.CredentialsProvider
import javax.inject.Inject
import play.api.i18n.I18nSupport
import play.api.libs.json.JsBoolean
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}

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
  emailService: EmailService,
  passwordHasherRegistry: PasswordHasherRegistry,
  authTokenService: ResetPasswordTokenService
)(
  implicit
  silhouette: Silhouette[AuthEnv],
  cookieUtils: CookieUtils,
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
  def sendResetEmail = silhouette.UnsecuredAction.async(parse.json[ResetPasswordRequest])({
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


  /**
    * Resets the password.
    *
    * @param token The token to identify a user.
    * @return The result to display.
    */
  def restorePassword(token: UUID) = silhouette.UnsecuredAction.async(parse.json[RestorePasswordRequest]) { implicit request =>
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
