package com.clemble.loveit.payment.model

import play.api.libs.json.{JsObject, Json}

case class BraintreeRequest(nonce: String, money: Money, details: Option[JsObject])

object BraintreeRequest {

  implicit val jsonFormat = Json.format[BraintreeRequest]

}
