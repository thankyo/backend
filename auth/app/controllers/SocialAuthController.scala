package controllers

import javax.inject.{Inject, Singleton}

import com.clemble.loveit.common.controller.CookieUtils
import com.clemble.loveit.common.util.AuthEnv
import com.clemble.loveit.user.model.{User}
import com.clemble.loveit.user.service.UserService
import com.mohiva.play.silhouette.api._
import com.mohiva.play.silhouette.api.exceptions.ProviderException
import com.mohiva.play.silhouette.api.repositories.AuthInfoRepository
import com.mohiva.play.silhouette.impl.providers._
import play.api.libs.json.{JsObject, JsString, Json}
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
                                       userService: UserService,
                                       authInfoRepository: AuthInfoRepository,
                                       socialProviderRegistry: SocialProviderRegistry,
                                       implicit val silhouette: Silhouette[AuthEnv],
                                       implicit val ec: ExecutionContext
) extends Controller with Logger {

  /**
    * Authenticates a user against a social provider.
    *
    * @param provider The ID of the provider to authenticate against.
    * @return The result to display.
    */
  def authenticate(provider: String) = Action.async{ implicit req => {

    def createAuthResult(user: User, loginInfo: LoginInfo, authInfo: AuthInfo): Future[Result] = {
      for {
        _ <- authInfoRepository.save(loginInfo, authInfo)
        res <- AuthUtils.authResponse(user, loginInfo)
      } yield {
        res
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
        result <- createAuthResult(user, profile.loginInfo, authInfo)
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
