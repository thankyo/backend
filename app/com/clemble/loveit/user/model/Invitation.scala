package com.clemble.loveit.user.model

import com.clemble.loveit.common.model.{CreatedAware, UserID}
import org.joda.time.DateTime
import play.api.libs.json.Json

case class Invitation(
                       linkOrEmail: String,
                       sender: UserID,
                       created: DateTime = DateTime.now()
                     ) extends CreatedAware

object Invitation {

  implicit val jsonFormat = Json.format[Invitation]

}
