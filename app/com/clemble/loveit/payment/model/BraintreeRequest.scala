package com.clemble.loveit.payment.model

import play.api.libs.json.{JsObject, Json}

case class BraintreeRequest(`type`: String, nonce: String, money: Money, details: JsObject)

object BraintreeRequest {

  implicit val jsonFormat = Json.format[BraintreeRequest]

}
