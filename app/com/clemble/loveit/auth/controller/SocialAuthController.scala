package com.clemble.loveit.auth.controller

import com.clemble.loveit.auth.service.AuthService
import com.clemble.loveit.common.controller.{CookieUtils, LoveItController}
import com.clemble.loveit.common.error.FieldValidationError
import com.clemble.loveit.common.service.TumblrProvider
import com.clemble.loveit.common.util.AuthEnv
import com.mohiva.play.silhouette.api._
import com.mohiva.play.silhouette.api.repositories.AuthInfoRepository
import com.mohiva.play.silhouette.impl.providers._
import javax.inject.{Inject, Singleton}
import play.api.libs.oauth._
import play.api.libs.ws.{EmptyBody, WSClient}
import play.api.mvc._
import scalaj.http.{Http, Token}

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
  wsClient: WSClient,
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
        case Some(p: TumblrProvider) if (req.queryString.isEmpty) =>
          val settings = p.settings
          val consumer = Token(settings.consumerKey, settings.consumerSecret)
          val response = Http(settings.requestTokenURL)
            .postForm(Seq("oauth_callback" -> settings.callbackURL))
            .oauth(consumer)
            .asString

          val consumerKey = ConsumerKey(settings.consumerKey, settings.consumerSecret)
          val token = RequestToken(null, null)
          val oAuthCalculator = OAuthCalculator(consumerKey, token)

          wsClient.url(settings.requestTokenURL)
            .sign(oAuthCalculator)
            .withHttpHeaders("Content-Length" -> "0")
            .post(Map.empty[String, String])
            .map(res => {
              println(response)
              println(res.body)
            }).map(_ => {
              BadRequest
            })

//          val oauth = OAuth(ServiceInfo(
//            settings.requestTokenURL,
//            settings.accessTokenURL,
//            settings.authorizationURL, consumerKey),
//            true
//          )
//
//          oauth.retrieveRequestToken(settings.callbackURL) match {
//            case Right(t) =>
//              // We received the unauthorized tokens in the OAuth object - store it before we proceed
//              Redirect(oauth.redirectUrl(t.token))
//            case Left(e) =>
//              throw e
//          }
        //Future.successful(Redirect(s"${p.settings.authorizationURL}?oauth_token=${response.body.key}"))
        case Some(p: SocialProvider) =>
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
