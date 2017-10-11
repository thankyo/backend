package com.clemble.loveit.auth.model.requests

import play.api.libs.json.Json

case class ChangePasswordRequest(
                                  currentPassword: String,
                                  newPassword: String
                                )

object ChangePasswordRequest {

  implicit val jsonFormat = Json.format[ChangePasswordRequest]

}