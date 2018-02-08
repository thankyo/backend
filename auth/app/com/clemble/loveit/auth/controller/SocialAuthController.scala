package com.clemble.loveit.auth.controller

import javax.inject.{Inject, Singleton}

import com.clemble.loveit.auth.service.AuthService
import com.clemble.loveit.common.controller.{CookieUtils, LoveItController}
import com.clemble.loveit.common.error.FieldValidationError
import com.clemble.loveit.common.util.AuthEnv
import com.mohiva.play.silhouette.api._
import com.mohiva.play.silhouette.api.repositories.AuthInfoRepository
import com.mohiva.play.silhouette.impl.providers._
import com.mohiva.play.silhouette.impl.providers.state.UserStateItem
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
                                    ) extends LoveItController(components) with Logger {

  /**
    * Authenticates a user against a social provider.
    *
    * @param provider The ID of the provider to authenticate against.
    * @return The result to display.
    */
  def authenticate(provider: String) = Action.async {
    implicit req => {
      val providerOpt = socialProviderRegistry.get[SocialProvider](provider)
      val user = cookieUtils.readUser(req)
      providerOpt match {
        case Some(p: SocialProvider with CommonSocialProfileBuilder) =>
          p.authenticate().flatMap({
            case Left(redirect) =>
              Future.successful(redirect)
            case Right(authInfo) =>
              val fSocialReg = authService.registerSocial(p)(authInfo, user)
              fSocialReg.flatMap(AuthUtils.authResponse)
          })
        case _ =>
          Future.successful(BadRequest(FieldValidationError("providerId", s"Cannot authenticate with unexpected social provider $provider")))
      }
    }
  }

}
