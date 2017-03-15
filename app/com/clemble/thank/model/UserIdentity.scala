package com.clemble.thank.model

import com.mohiva.play.silhouette.api.Identity
import org.joda.time.DateTime
import play.api.libs.json.Json

case class UserIdentity(
                         id: UserID,
                         firstName: Option[String],
                         lastName: Option[String],
                         thumbnail: Option[String],
                         dateOfBirth: Option[DateTime]
                       ) extends UserProfile with Identity

object UserIdentity {

  implicit val format = Json.format[UserIdentity]

}
