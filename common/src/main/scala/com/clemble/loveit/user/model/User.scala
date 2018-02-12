package com.clemble.loveit.user.model

import java.time.{LocalDate, LocalDateTime}

import com.clemble.loveit.common.model._
import com.clemble.loveit.common.util.WriteableUtils
import com.mohiva.play.silhouette.api.{Identity, LoginInfo}
import com.mohiva.play.silhouette.impl.providers.{CommonSocialProfile, CredentialsProvider}
import com.mohiva.play.silhouette.impl.providers.oauth2.{FacebookProvider, GoogleProvider}
import play.api.http.Writeable
import play.api.libs.json.{Json, OFormat}

case class UserSocialConnections(
                                  credentials: Option[String] = None,
                                  facebook: Option[String] = None,
                                  google: Option[String] = None
                                ) {

  def asGoogleLogin() = google.map(LoginInfo(GoogleProvider.ID, _))

  def asFacebookLogin() = facebook.map(LoginInfo(FacebookProvider.ID, _))

  def asCredentialsLogin() = credentials.map(LoginInfo(CredentialsProvider.ID, _))

}

/**
  * User abstraction
  *
  * User has few roles
  * - Gratitude giver
  *    - give thanks, to different urls
  * - Gratitude receiver
  *    - receives thanks from owned urls
  */
case class User(
                 id: UserID,
                 firstName: Option[String] = None,
                 lastName: Option[String] = None,
                 email: Email,
                 avatar: Option[String] = None,
                 bio: Option[String] = None,
                 dateOfBirth: Option[LocalDate] = None,
                 profiles: UserSocialConnections = UserSocialConnections(),
                 created: LocalDateTime = LocalDateTime.now()
               ) extends Identity with CreatedAware {

  /**
    * Tries to construct a name.
    *
    * @return Maybe a name.
    */
  def name: Option[String] = firstName -> lastName match {
    case (Some(f), Some(l)) => Some(f + " " + l)
    case (_, _) => firstName.orElse(lastName)
  }

  def hasProvider(providerID: String): Boolean = {
    providerID match {
      case FacebookProvider.ID => profiles.facebook.isDefined
      case GoogleProvider.ID => profiles.google.isDefined
      case CredentialsProvider.ID => profiles.credentials.isDefined
    }
  }

  def link(socialProfile: CommonSocialProfile): User = {
    this.copy(
      firstName = firstName.orElse(socialProfile.firstName),
      lastName = lastName.orElse(socialProfile.lastName),
      // following #60 we should ignore avatar from profile for now
      profiles = socialProfile.loginInfo.providerID match {
        case FacebookProvider.ID => profiles.copy(facebook = Some(socialProfile.loginInfo.providerKey))
        case GoogleProvider.ID => profiles.copy(google = Some(socialProfile.loginInfo.providerKey))
        case CredentialsProvider.ID => profiles.copy(credentials = Some(socialProfile.loginInfo.providerKey))
      }
    )
  }

}

trait UserAware {
  val user: UserID
}

object User {

  val UNKNOWN = "UNKNOWN"

  implicit val socialProfileJsonFormat: OFormat[CommonSocialProfile] = Json.format[CommonSocialProfile]
  implicit val userSocialConnections: OFormat[UserSocialConnections] = Json.format[UserSocialConnections]
  implicit val jsonFormat: OFormat[User] = Json.format[User]

  implicit val userWriteable: Writeable[User] = WriteableUtils.jsonToWriteable[User]

}
