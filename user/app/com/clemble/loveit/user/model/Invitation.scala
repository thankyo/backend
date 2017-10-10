package com.clemble.loveit.user.model

import java.time.LocalDateTime

import com.clemble.loveit.common.model.{CreatedAware, UserID}
import play.api.libs.json.Json

case class Invitation(
                       linkOrEmail: String,
                       sender: UserID,
                       created: LocalDateTime = LocalDateTime.now()
                     ) extends CreatedAware

object Invitation {

  implicit val jsonFormat = Json.format[Invitation]

}
