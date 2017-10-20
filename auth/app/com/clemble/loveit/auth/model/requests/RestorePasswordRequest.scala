package com.clemble.loveit.auth.model.requests

import play.api.libs.json.Json

case class RestorePasswordRequest(
                                 password: String
                               )

object RestorePasswordRequest {

  implicit val json = Json.format[RestorePasswordRequest]

}