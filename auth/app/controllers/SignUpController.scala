package controllers

import javax.inject.Inject

import com.clemble.loveit.common.util.{AuthEnv, IDGenerator}
import com.clemble.loveit.user.model.User
import com.clemble.loveit.user.service.UserService
import com.mohiva.play.silhouette.api._
import com.mohiva.play.silhouette.api.repositories.AuthInfoRepository
import com.mohiva.play.silhouette.api.services.AvatarService
import com.mohiva.play.silhouette.api.util.{Credentials, PasswordHasherRegistry}
import com.mohiva.play.silhouette.impl.providers._
import forms.SignUpRequest
import models.services.AuthTokenService
import play.api.i18n.{I18nSupport, Messages}
import play.api.libs.mailer.{Email, MailerClient}
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
                                  parse: PlayBodyParsers,
                                  ex: ExecutionContext
                                ) extends AbstractController(components) with I18nSupport {

  /**
    * Handles the submitted form.
    *
    * @return The result to display.
    */
  def submit = silhouette.UnsecuredAction.async(parse.json[SignUpRequest]) { implicit request: Request[SignUpRequest] =>
    val data = request.body
    val loginInfo = LoginInfo(CredentialsProvider.ID, data.email)
    userService.retrieve(loginInfo).flatMap {
      case Some(_) =>
        Future.successful(BadRequest("Email already signedUp"))
      case None =>
        val authInfo = passwordHasherRegistry.current.hash(data.password)
        val user = new User(
          id = IDGenerator.generate(),
          firstName = Some(data.firstName),
          lastName = Some(data.lastName),
          email = data.email,
          avatarURL = None,
          profiles = Set(loginInfo)
        )
        for {
          avatar <- avatarService.retrieveURL(data.email)
          user <- userService.save(user.copy(avatarURL = avatar))
          _ <- authInfoRepository.add(loginInfo, authInfo)
          res <- AuthUtils.authResponse(user, loginInfo)
        } yield {
          res
        }
    }
  }
}
