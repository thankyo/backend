package com.clemble.loveit.common.service

import com.clemble.loveit.common.service.TumblrProvider._
import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.api.util.HTTPLayer
import com.mohiva.play.silhouette.impl.exceptions.ProfileRetrievalException
import com.mohiva.play.silhouette.impl.providers._
import play.api.libs.json.JsValue

import scala.concurrent.Future

/**
  * Base Tumblr OAuth1 Provider.
  *
  * @see https://www.tumblr.com/docs/en/api/v2#user-methods
  */
trait BaseTumblrProvider extends OAuth1Provider {

  /**
    * The content type to parse a profile from.
    */
  override type Content = JsValue

  /**
    * The provider ID.
    */
  override val id = ID

  /**
    * Defines the URLs that are needed to retrieve the profile data.
    */
  override protected val urls = Map("api" -> settings.apiURL.getOrElse(API))

  /**
    * Builds the social profile.
    *
    * @param authInfo The auth info received from the provider.
    * @return On success the build social profile, otherwise a failure.
    */
  override protected def buildProfile(authInfo: OAuth1Info): Future[Profile] = {
    httpLayer.url(urls("api")).sign(service.sign(authInfo)).get().flatMap { response =>
      val json = response.json
      (json \ "errors" \\ "code").headOption.map(_.as[Int]) match {
        case Some(code) =>
          val message = (json \ "errors" \\ "message").headOption.map(_.as[String])

          Future.failed(new ProfileRetrievalException(SpecifiedProfileError.format(id, code, message)))
        case _ => profileParser.parse(json, authInfo)
      }
    }
  }
}

/**
  * The profile parser for the common social profile.
  */
class TumblrProfileParser extends SocialProfileParser[JsValue, CommonSocialProfile, OAuth1Info] {

  /**
    * Parses the social profile.
    *
    * @param json     The content returned from the provider.
    * @param authInfo The auth info to query the provider again for additional data.
    * @return The social profile from given result.
    */
  override def parse(json: JsValue, authInfo: OAuth1Info) = Future.successful {
    val userID = (json \ "id").as[Long]
    val fullName = (json \ "name").asOpt[String]

    CommonSocialProfile(
      loginInfo = LoginInfo(ID, userID.toString),
      fullName = fullName
    )
  }
}

/**
  * The Tumblr OAuth1 Provider.
  *
  * @param httpLayer           The HTTP layer implementation.
  * @param service             The OAuth1 service implementation.
  * @param tokenSecretProvider The OAuth1 token secret provider implementation.
  * @param settings            The OAuth1 provider settings.
  */
class TumblrProvider(
  protected val httpLayer: HTTPLayer,
  val service: OAuth1Service,
  protected val tokenSecretProvider: OAuth1TokenSecretProvider,
  val settings: OAuth1Settings)
  extends BaseTumblrProvider with CommonSocialProfileBuilder {

  /**
    * The type of this class.
    */
  override type Self = TumblrProvider

  /**
    * The profile parser implementation.
    */
  override val profileParser = new TumblrProfileParser

  /**
    * Gets a provider initialized with a new settings object.
    *
    * @param f A function which gets the settings passed and returns different settings.
    * @return An instance of the provider initialized with new settings.
    */
  override def withSettings(f: (Settings) => Settings) = {
    new TumblrProvider(httpLayer, service.withSettings(f), tokenSecretProvider, f(settings))
  }
}

/**
  * The companion object.
  */
object TumblrProvider {

  /**
    * The error messages.
    */
  val SpecifiedProfileError = "[Silhouette][%s] error retrieving profile information. Error code: %s, message: %s"

  /**
    * The Tumblr constants.
    */
  val ID = "tumblr"

  val API = "https://api.tumblr.com/v2/user/info"
}

