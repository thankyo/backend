package com.clemble.loveit.auth.service

import play.api.libs.oauth.{RequestToken, ServiceInfo}
import play.shaded.oauth.oauth.signpost.basic.DefaultOAuthConsumer
import play.shaded.oauth.oauth.signpost.exception.OAuthException

case class AnotherOAuth(info: ServiceInfo, use10a: Boolean = true) {

  private val provider = {
    val p = new AnotherOAuthProvider(info.requestTokenURL, info.accessTokenURL, info.authorizationURL)
    p.setOAuth10a(use10a)
    p
  }

  /**
    * Request the request token and secret.
    *
    * @param callbackURL the URL where the provider should redirect to (usually a URL on the current app)
    * @return A Right(RequestToken) in case of success, Left(OAuthException) otherwise
    */
  def retrieveRequestToken(callbackURL: String): Either[OAuthException, RequestToken] = {
    val consumer = new DefaultOAuthConsumer(info.key.key, info.key.secret)
    try {
      provider.retrieveRequestToken(consumer, callbackURL)
      Right(RequestToken(consumer.getToken(), consumer.getTokenSecret()))
    } catch {
      case e: OAuthException => Left(e)
    }
  }

  /**
    * Exchange a request token for an access token.
    *
    * @param token    the token/secret pair obtained from a previous call
    * @param verifier a string you got through your user, with redirection
    * @return A Right(RequestToken) in case of success, Left(OAuthException) otherwise
    */
  def retrieveAccessToken(token: RequestToken, verifier: String): Either[OAuthException, RequestToken] = {
    val consumer = new DefaultOAuthConsumer(info.key.key, info.key.secret)
    consumer.setTokenWithSecret(token.token, token.secret)
    try {
      provider.retrieveAccessToken(consumer, verifier)
      Right(RequestToken(consumer.getToken(), consumer.getTokenSecret()))
    } catch {
      case e: OAuthException => Left(e)
    }
  }

  /**
    * The URL where the user needs to be redirected to grant authorization to your application.
    *
    * @param token request token
    */
  def redirectUrl(token: String): String = {
    import play.shaded.oauth.oauth.signpost.{OAuth => O}
    O.addQueryParameters(
      provider.getAuthorizationWebsiteUrl(),
      O.OAUTH_TOKEN,
      token
    )
  }

}
