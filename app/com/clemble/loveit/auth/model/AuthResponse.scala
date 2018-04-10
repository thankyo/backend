package com.clemble.loveit.auth.model

import com.clemble.loveit.common.util.WriteableUtils
import play.api.libs.json.Json

case class AuthResponse(
                         token: String,
                         existing: Boolean
                       )

object AuthResponse {

  implicit val jsonFormat = Json.format[AuthResponse]
  implicit val writeable = WriteableUtils.jsonToWriteable[AuthResponse]

}
