package com.clemble.loveit.user.model

import java.time.LocalDateTime

import com.clemble.loveit.common.model.UserID
import com.mohiva.play.silhouette.api.Identity
import play.api.libs.json.Json

case class UserIdentity(
                         id: UserID,
                         firstName: Option[String],
                         lastName: Option[String],
                         thumbnail: Option[String],
                         dateOfBirth: Option[LocalDateTime]
                       ) extends UserProfile with Identity

object UserIdentity {

  implicit val jsonFormat = Json.format[UserIdentity]

}
