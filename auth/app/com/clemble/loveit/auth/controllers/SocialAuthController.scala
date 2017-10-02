package com.clemble.loveit.auth.controllers

import javax.inject.{Inject, Singleton}

import com.clemble.loveit.common.util.AuthEnv
import com.clemble.loveit.user.service.UserService
import com.mohiva.play.silhouette.api._
import com.mohiva.play.silhouette.api.exceptions.ProviderException
import com.mohiva.play.silhouette.api.repositories.AuthInfoRepository
import com.mohiva.play.silhouette.impl.providers._
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}

/**
  * The social auth controller.
  *
  * @param silhouette The Silhouette stack.
  * @param authInfoRepository The auth info service implementation.
  * @param socialProviderRegistry The social provider registry.
  */
@Singleton
class SocialAuthController @Inject() (
                                       components: ControllerComponents,
                                       userService: UserService,
                                       authInfoRepository: AuthInfoRepository,
                                       socialProviderRegistry: SocialProviderRegistry)
                                      (
                                       implicit
                                       silhouette: Silhouette[AuthEnv],
                                       ec: ExecutionContext)
  extends AbstractController(components) with Logger {

  /**
    * Authenticates a user against a social provider.
    *
    * @param provider The ID of the provider to authenticate against.
    * @return The result to display.
    */
  def authenticate(provider: String) = Action.async{ implicit req => {

    def processProviderResponse(p: SocialProvider with CommonSocialProfileBuilder)(authInfo: p.A): Future[Result] = {
      for {
        profile <- p.retrieveProfile(authInfo)
        eitherUser <- userService.createOrUpdateUser(profile)
        _ <- authInfoRepository.save(profile.loginInfo, authInfo)
        result <- AuthUtils.authResponse(eitherUser, profile.loginInfo)
      } yield {
        result
      }
    }

    (socialProviderRegistry.get[SocialProvider](provider) match {
      case Some(p: SocialProvider with CommonSocialProfileBuilder) =>
        p.authenticate().flatMap {
          case Left(redirect) =>
            Future.successful(redirect)
          case Right(authInfo) =>
            processProviderResponse(p)(authInfo)
        }
      case _ => {
        Future.failed(new ProviderException(s"Cannot authenticate with unexpected social provider $provider"))
      }
    }).recover {
      case e: ProviderException =>
        logger.error("Unexpected provider error", e)
        Redirect("/")
    }
  }}

}
