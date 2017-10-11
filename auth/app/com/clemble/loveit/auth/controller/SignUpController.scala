package com.clemble.loveit.auth.controller

import javax.inject.Inject

import com.clemble.loveit.common.util.{AuthEnv, IDGenerator}
import com.clemble.loveit.user.model.User
import com.clemble.loveit.user.service.UserService
import com.mohiva.play.silhouette.api._
import com.mohiva.play.silhouette.api.repositories.AuthInfoRepository
import com.mohiva.play.silhouette.api.services.AvatarService
import com.mohiva.play.silhouette.api.util.{PasswordHasherRegistry, PasswordInfo}
import com.mohiva.play.silhouette.impl.providers._
import com.clemble.loveit.auth.model.requests.SignUpRequest
import com.clemble.loveit.auth.service.AuthTokenService
import play.api.i18n.I18nSupport
import play.api.libs.mailer.MailerClient
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}

/**
  * The `Sign Up` controller.
  *
  * @param components             The Play controller components.
  * @param silhouette             The Silhouette stack.
  * @param userService            The user service implementation.
  * @param authInfoRepository     The auth info repository implementation.
  * @param authTokenService       The auth token service implementation.
  * @param avatarService          The avatar service implementation.
  * @param passwordHasherRegistry The password hasher registry.
  * @param mailerClient           The mailer client.
  * @param ex                     The execution context.
  */
class SignUpController @Inject()(
                                  components: ControllerComponents,
                                  userService: UserService,
                                  authInfoRepository: AuthInfoRepository,
                                  authTokenService: AuthTokenService,
                                  avatarService: AvatarService,
                                  passwordHasherRegistry: PasswordHasherRegistry,
                                  mailerClient: MailerClient
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
  def submit: Action[SignUpRequest] = silhouette.UnsecuredAction.async(parse.json[SignUpRequest]) { implicit req: Request[SignUpRequest] =>
    val signUp = req.body
    val loginInfo = LoginInfo(CredentialsProvider.ID, signUp.email)
    authInfoRepository.find[PasswordInfo](loginInfo).flatMap {
      case Some(_) =>
        Future.successful(BadRequest("Email already signedUp"))
      case None =>
        val authInfo = passwordHasherRegistry.current.hash(signUp.password)
        val user = signUp.toUser()
        for {
          avatar <- avatarService.retrieveURL(signUp.email)
          user <- userService.save(user.copy(avatar = avatar))
          _ <- authInfoRepository.save(loginInfo, authInfo)
          res <- AuthUtils.authResponse(Right(user), loginInfo)
        } yield {
          res
        }
    }
  }
}
