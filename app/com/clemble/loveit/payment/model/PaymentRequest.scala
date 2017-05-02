package com.clemble.loveit.payment.model

import play.api.libs.json.{JsObject, Json}

case class PaymentRequest(nonce: String, money: Money, details: Option[JsObject])

object PaymentRequest {

  implicit val jsonFormat = Json.format[PaymentRequest]

}
