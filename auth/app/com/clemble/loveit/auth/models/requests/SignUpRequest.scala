package com.clemble.loveit.auth.models.requests

import com.clemble.loveit.common.model.Email
import play.api.libs.json.Json

case class SignUpRequest(
                          firstName: String,
                          lastName: String,
                          email: Email,
                          password: String
                        )

object SignUpRequest {

  implicit val json = Json.format[SignUpRequest]

}