package com.clemble.thank.social.auth


import javax.inject.Inject

import com.clemble.thank.model.{User, UserIdentity}
import com.clemble.thank.service.repository.UserRepository
import com.clemble.thank.util.AuthEnv
import com.mohiva.play.silhouette.api._
import com.mohiva.play.silhouette.api.exceptions.ProviderException
import com.mohiva.play.silhouette.api.repositories.AuthInfoRepository
import com.mohiva.play.silhouette.impl.providers._
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, Controller, Result}

import scala.concurrent.{ExecutionContext, Future}

/**
  * The social auth controller.
  *
  * @param messagesApi The Play messages API.
  * @param silhouette The Silhouette stack.
  * @param userRepo The user service implementation.
  * @param authInfoRepository The auth info service implementation.
  * @param socialProviderRegistry The social provider registry.
  */
class SocialAuthController @Inject() (
                                       val messagesApi: MessagesApi,
                                       silhouette: Silhouette[AuthEnv],
                                       userRepo: UserRepository,
                                       authInfoRepository: AuthInfoRepository,
                                       socialProviderRegistry: SocialProviderRegistry,
                                       implicit val ec: ExecutionContext
) extends Controller with I18nSupport with Logger {

  /**
    * Authenticates a user against a social provider.
    *
    * @param provider The ID of the provider to authenticate against.
    * @return The result to display.
    */
  def authenticate(provider: String) = Action.async{ implicit req => {
    def createOrUpdateUser(profile: CommonSocialProfile): Future[UserIdentity] = {
      for {
        existingUserOpt <- userRepo.retrieve(profile.loginInfo)
        user <- existingUserOpt.
          map(identity => Future.successful(identity)).
          getOrElse(userRepo.save(User from profile).map(_.toIdentity()))
      } yield {
        logger.debug(s"${if (existingUserOpt.isDefined) "NEW user created" else "Using existing user"}")
        user
      }
    }

    def createAuthResult(profile: CommonSocialProfile, authInfo: AuthInfo): Future[Result] = {
      for {
        _ <- authInfoRepository.save(profile.loginInfo, authInfo)
        authenticator <- silhouette.env.authenticatorService.create(profile.loginInfo)
        value <- silhouette.env.authenticatorService.init(authenticator)
        result <- silhouette.env.authenticatorService.embed(value, Ok(value))
      } yield {
        result
      }
    }

    (socialProviderRegistry.get[SocialProvider](provider) match {
      case Some(p: SocialProvider with CommonSocialProfileBuilder) =>
        p.authenticate().flatMap {
          case Left(result) =>
            Future.successful(result)
          case Right(authInfo) => for {
            profile <- p.retrieveProfile(authInfo)
            user <- createOrUpdateUser(profile)
            result <- createAuthResult(profile, authInfo)
          } yield {
            silhouette.env.eventBus.publish(LoginEvent(user, req))
            result
          }
        }
      case _ => {
        Future.failed(new ProviderException(s"Cannot authenticate with unexpected social provider $provider"))
      }
    }).recover {
      case e: ProviderException =>
        logger.error("Unexpected provider error", e)
        Redirect("/join")
    }
  }}

}
