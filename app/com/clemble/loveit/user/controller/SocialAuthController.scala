package com.clemble.loveit.user.controller

import javax.inject.{Inject, Singleton}

import com.clemble.loveit.common.util.AuthEnv
import com.clemble.loveit.user.model.{User, UserIdentity}
import com.clemble.loveit.user.service.repository.UserRepository
import com.mohiva.play.silhouette.api._
import com.mohiva.play.silhouette.api.exceptions.ProviderException
import com.mohiva.play.silhouette.api.repositories.AuthInfoRepository
import com.mohiva.play.silhouette.impl.providers._
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.Json
import play.api.mvc._

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
@Singleton
class SocialAuthController @Inject() (
                                       val messagesApi: MessagesApi,
                                       silhouette: Silhouette[AuthEnv],
                                       userRepo: UserRepository,
                                       authInfoRepository: AuthInfoRepository,
                                       socialProviderRegistry: SocialProviderRegistry,
                                       implicit val ec: ExecutionContext
) extends Controller with I18nSupport with Logger {

  def createOrUpdateUser(profile: CommonSocialProfile)(implicit header: RequestHeader): Future[UserIdentity] = {
    for {
      existingUserOpt <- userRepo.retrieve(profile.loginInfo)
      user <- existingUserOpt.
        map(identity => Future.successful(identity)).
        getOrElse(userRepo.save(User from profile).map(_.toIdentity()))
    } yield {
      if (existingUserOpt.isEmpty) {
        silhouette.env.eventBus.publish(SignUpEvent(user, header))
      } else {
        silhouette.env.eventBus.publish(LoginEvent(user, header))
      }
      logger.debug(s"${if (existingUserOpt.isDefined) "NEW user created" else "Using existing user"}")
      user
    }
  }

  /**
    * Authenticates a user against a social provider.
    *
    * @param provider The ID of the provider to authenticate against.
    * @return The result to display.
    */
  def authenticate(provider: String) = Action.async{ implicit req => {

    def createAuthResult(user: UserIdentity, profile: CommonSocialProfile, authInfo: AuthInfo): Future[Result] = {
      for {
        _ <- authInfoRepository.save(profile.loginInfo, authInfo)
        authenticator <- silhouette.env.authenticatorService.create(profile.loginInfo)
        authenticatorWithClaim = authenticator.copy(customClaims = Some(Json.obj("user" -> user.id)))
        value <- silhouette.env.authenticatorService.init(authenticatorWithClaim)
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
            result <- createAuthResult(user, profile, authInfo)
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
