package com.clemble.loveit.auth.controllers

import javax.inject.Inject

import com.clemble.loveit.common.util.AuthEnv
import com.clemble.loveit.user.service.UserService
import com.mohiva.play.silhouette.api._
import com.mohiva.play.silhouette.api.util.{Clock, Credentials}
import com.mohiva.play.silhouette.impl.exceptions.IdentityNotFoundException
import com.mohiva.play.silhouette.impl.providers._
import play.api.Configuration
import play.api.i18n.I18nSupport
import play.api.libs.json.Json
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}

/**
  * The `Sign In` controller.
  *
  * @param components             The Play controller components.
  * @param silhouette             The Silhouette stack.
  * @param userService            The user service implementation.
  * @param credentialsProvider    The credentials provider.
  * @param socialProviderRegistry The social provider registry.
  * @param configuration          The Play configuration.
  * @param clock                  The clock instance.
  */
class SignInController @Inject()(
                                  userService: UserService,
                                  credentialsProvider: CredentialsProvider,
                                  socialProviderRegistry: SocialProviderRegistry,
                                  configuration: Configuration,
                                  components: ControllerComponents,
                                  clock: Clock
                                )(
                                  implicit
                                  silhouette: Silhouette[AuthEnv],
                                  parser: PlayBodyParsers,
                                  ex: ExecutionContext
                                ) extends AbstractController(components) with I18nSupport {


  implicit val credentialsJson = Json.format[Credentials]

  /**
    * Handles the submitted form.
    *
    * @return The result to display.
    */
  def submit = silhouette.UnsecuredAction.async(parse.json[Credentials]) { implicit req: Request[Credentials] =>
    val credentials = req.body
    credentialsProvider.authenticate(credentials).flatMap { loginInfo =>
      userService.retrieve(loginInfo).flatMap {
        case Some(user) =>
          AuthUtils.authResponse(Left(user), loginInfo)
        case None =>
          Future.failed(new IdentityNotFoundException("Couldn't find user"))
      }
    }
  }

}
