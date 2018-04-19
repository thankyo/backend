package com.clemble.loveit.auth.controller

import com.clemble.loveit.auth.service.AuthService
import com.clemble.loveit.common.controller.{CookieUtils, LoveItController}
import com.clemble.loveit.common.error.FieldValidationError
import com.clemble.loveit.common.service.TumblrProvider
import com.clemble.loveit.common.util.AuthEnv
import com.github.scribejava.apis.TumblrApi
import com.github.scribejava.core.model.{OAuth1AccessToken, OAuth1RequestToken, OAuthAsyncRequestCallback}
import com.mohiva.play.silhouette.api._
import com.mohiva.play.silhouette.api.repositories.AuthInfoRepository
import com.mohiva.play.silhouette.api.util.ExtractableRequest
import com.mohiva.play.silhouette.impl.providers._
import com.mohiva.play.silhouette.impl.providers.oauth1.secrets.{CookieSecret, CookieSecretProvider}
import javax.inject.{Inject, Singleton}
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future, Promise}

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
  tokenSecretProvider: CookieSecretProvider,
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
        case Some(p: TumblrProvider) =>
          val settings = p.settings
          import com.github.scribejava.core.builder.ServiceBuilder
          val service = new ServiceBuilder(settings.consumerKey)
            .apiSecret(settings.consumerSecret)
            .callback(settings.callbackURL)
            .build(TumblrApi.instance())


          if (req.queryString.isEmpty) {
            val fRequestToken = Promise[OAuth1RequestToken]()

            service.getRequestTokenAsync(new OAuthAsyncRequestCallback[OAuth1RequestToken] {
              override def onCompleted(response: OAuth1RequestToken): Unit = fRequestToken success response
              override def onThrowable(t: Throwable): Unit = fRequestToken failure t
            })

            for {
              token <- fRequestToken.future
              secret <- tokenSecretProvider.build(OAuth1Info(token.getToken, token.getTokenSecret))
            } yield {
              val redirect = Redirect(service.getAuthorizationUrl(token))
              tokenSecretProvider.publish(redirect, secret)
            }
          } else {
            val extrReq: ExtractableRequest[_] = req
            val token = extrReq.extractString(OAuth1Provider.OAuthToken)
            val verifier = extrReq.extractString(OAuth1Provider.OAuthVerifier)

            val fAccessToken = Promise[OAuth1AccessToken]()
            tokenSecretProvider.retrieve.map(tokenSecret => {
              service.getAccessTokenAsync(new OAuth1RequestToken(token.get, tokenSecret.value), verifier.get, new OAuthAsyncRequestCallback[OAuth1AccessToken] {
                override def onCompleted(accessToken: OAuth1AccessToken): Unit = {
                  fAccessToken success accessToken
                }
                override def onThrowable(t: Throwable): Unit = {
                  fAccessToken failure t
                }
              })
            })

            fAccessToken.future.flatMap(accessToken => {
              val authInfo = OAuth1Info(accessToken.getToken, accessToken.getTokenSecret)
              val fSocialReg = authService.registerSocial(p)(authInfo, user)
              fSocialReg.flatMap(AuthUtils.authResponse)
            })
          }
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
