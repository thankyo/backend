package com.clemble.loveit.user.controller

import javax.inject.{Inject, Singleton}

import com.clemble.loveit.common.controller.CookieUtils
import com.clemble.loveit.common.util.AuthEnv
import com.clemble.loveit.user.model.{UserIdentity}
import com.clemble.loveit.user.service.UserService
import com.mohiva.play.silhouette.api._
import com.mohiva.play.silhouette.api.exceptions.ProviderException
import com.mohiva.play.silhouette.api.repositories.AuthInfoRepository
import com.mohiva.play.silhouette.impl.providers._
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.{JsObject, JsString, Json}
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}

/**
  * The social auth controller.
  *
  * @param messagesApi The Play messages API.
  * @param silhouette The Silhouette stack.
  * @param authInfoRepository The auth info service implementation.
  * @param socialProviderRegistry The social provider registry.
  */
@Singleton
class SocialAuthController @Inject() (
                                       val messagesApi: MessagesApi,
                                       silhouette: Silhouette[AuthEnv],
                                       userService: UserService,
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

    def createAuthResult(user: UserIdentity, profile: CommonSocialProfile, authInfo: AuthInfo): Future[Result] = {
      val userDetails = Some(Json.toJson(user).as[JsObject])
      for {
        _ <- authInfoRepository.save(profile.loginInfo, authInfo)
        authenticator <- silhouette.env.authenticatorService.create(profile.loginInfo)
        authenticatorWithClaim = authenticator.copy(customClaims = userDetails)
        value <- silhouette.env.authenticatorService.init(authenticatorWithClaim)
        result <- silhouette.env.authenticatorService.embed(value, Ok(JsString(value)))
      } yield {
        CookieUtils.setUser(result, user.id)
      }
    }

    def processProviderResponse(p: SocialProvider with CommonSocialProfileBuilder)(authInfo: p.A): Future[Result] = {
      for {
        profile <- p.retrieveProfile(authInfo)
        eitherUser <- userService.createOrUpdateUser(profile)
        user = eitherUser match {
          case Left(user) => user
          case Right(user) => user
        }
        result <- createAuthResult(user, profile, authInfo)
      } yield {
        if (eitherUser.isRight) {
          silhouette.env.eventBus.publish(SignUpEvent(user, req))
        }
        silhouette.env.eventBus.publish(LoginEvent(user, req))
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
        Redirect("/join")
    }
  }}

}
