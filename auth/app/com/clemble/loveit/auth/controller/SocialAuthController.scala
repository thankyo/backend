package com.clemble.loveit.auth.controller

import javax.inject.{Inject, Singleton}

import com.clemble.loveit.auth.service.AuthService
import com.clemble.loveit.common.controller.CookieUtils
import com.clemble.loveit.common.util.AuthEnv
import com.mohiva.play.silhouette.api._
import com.mohiva.play.silhouette.api.exceptions.ProviderException
import com.mohiva.play.silhouette.api.repositories.AuthInfoRepository
import com.mohiva.play.silhouette.impl.providers._
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}

/**
  * The social auth controller.
  *
  * @param silhouette             The Silhouette stack.
  * @param authInfoRepository     The auth info service implementation.
  * @param socialProviderRegistry The social provider registry.
  */
@Singleton
class SocialAuthController @Inject()(
                                      authService: AuthService,
                                      authInfoRepository: AuthInfoRepository,
                                      socialProviderRegistry: SocialProviderRegistry,
                                      components: ControllerComponents,
                                    )(implicit
                                      ec: ExecutionContext,
                                      cookieUtils: CookieUtils,
                                      silhouette: Silhouette[AuthEnv]
                                    ) extends AbstractController(components) with Logger {

  /**
    * Authenticates a user against a social provider.
    *
    * @param provider The ID of the provider to authenticate against.
    * @return The result to display.
    */
  def authenticate(provider: String) = Action.async { implicit req => {
    (socialProviderRegistry.get[SocialProvider](provider) match {
      case Some(p: SocialProvider with CommonSocialProfileBuilder) =>
        p.authenticate().flatMap {
          case Left(redirect) =>
            Future.successful(redirect)
          case Right(authInfo) =>
            authService.registerSocial(p)(authInfo).flatMap(AuthUtils.authResponse)
        }
      case _ => {
        Future.failed(new ProviderException(s"Cannot authenticate with unexpected social provider $provider"))
      }
    }).recover {
      case e: ProviderException =>
        logger.error("Unexpected provider error", e)
        Redirect("/")
    }
  }
  }

}
