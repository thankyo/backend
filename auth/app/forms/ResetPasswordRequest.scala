package forms

import play.api.libs.json.Json

case class ResetPasswordRequest(
                                 password: String
                               )

object ResetPasswordRequest {

  implicit val json = Json.format[ResetPasswordRequest]

}