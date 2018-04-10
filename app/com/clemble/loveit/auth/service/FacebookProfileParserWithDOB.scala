package com.clemble.loveit.auth.service

import java.time.LocalDate

import com.clemble.loveit.common.model.{CommonSocialProfileWithDOB, CommonSocialProfileWithDOBBuilder}
import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.api.util.HTTPLayer
import com.mohiva.play.silhouette.impl.providers.oauth2.{BaseFacebookProvider, FacebookProfileParser, FacebookProvider}
import com.mohiva.play.silhouette.impl.providers._
import com.mohiva.play.silhouette.impl.providers.oauth2.FacebookProvider.ID
import play.api.libs.json.JsValue

import scala.concurrent.Future
import scala.util.Try

class FacebookProfileParserWithDOB extends SocialProfileParser[JsValue, CommonSocialProfileWithDOB, OAuth2Info] {

  override def parse(json: JsValue, authInfo: OAuth2Info) = Future.successful {
    val userID = (json \ "id").as[String]
    val firstName = (json \ "first_name").asOpt[String]
    val lastName = (json \ "last_name").asOpt[String]
    val fullName = (json \ "name").asOpt[String]
    val avatarURL = (json \ "picture" \ "data" \ "url").asOpt[String]
    val email = (json \ "email").asOpt[String]
    val dob = (json \ "birthday").asOpt[String].flatMap(dob => Try({
      val parts = dob.split("/")
      val month = if (parts.length >= 1) parts(0).toInt else 1980
      val day = if (parts.length >= 2) parts(1).toInt else 1
      val year = if (parts.length >= 3) parts(2).toInt else 1
      LocalDate.of(year, month, day)
    }).toOption)

    CommonSocialProfileWithDOB(
      loginInfo = LoginInfo(ID, userID),
      firstName = firstName,
      lastName = lastName,
      fullName = fullName,
      avatarURL = avatarURL,
      email = email,
      dateOfBirth = dob
    )
  }

}

class FacebookProviderWithDOB(
  protected val httpLayer: HTTPLayer,
  protected val stateHandler: SocialStateHandler,
  val settings: OAuth2Settings)
  extends BaseFacebookProvider with CommonSocialProfileWithDOBBuilder {

  override type Profile = CommonSocialProfileWithDOB

  /**
    * The type of this class.
    */
  override type Self = FacebookProviderWithDOB

  /**
    * The profile parser implementation.
    */
  override val profileParser: SocialProfileParser[JsValue, Profile, OAuth2Info] = new FacebookProfileParserWithDOB

  /**
    * Gets a provider initialized with a new settings object.
    *
    * @param f A function which gets the settings passed and returns different settings.
    * @return An instance of the provider initialized with new settings.
    */
  override def withSettings(f: (Settings) => Settings) = new FacebookProviderWithDOB(httpLayer, stateHandler, f(settings))
}
