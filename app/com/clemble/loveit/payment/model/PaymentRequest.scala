package com.clemble.loveit.payment.model

import play.api.data.validation.ValidationError
import play.api.libs.json._

sealed trait PaymentRequest {
  val nonce: String
  val money: Money
}

case class BraintreePaymentRequest(`type`: String, nonce: String, money: Money, details: Option[JsObject]) extends PaymentRequest
case class StripePaymentRequest(nonce: String, money: Money) extends PaymentRequest

object PaymentRequest {

  val payPalJsonFormat = Json.format[BraintreePaymentRequest]
  val stripeJsonFormat = Json.format[StripePaymentRequest]

  /**
    * JSON format for [[PaymentRequest]]
    */
  implicit val jsonFormat = new Format[PaymentRequest] {

    val PAY_PAL_TAG = JsString("payPal")
    val STRIPE_TAG = JsString("stripe")

    override def reads(json: JsValue): JsResult[PaymentRequest] = (json \ "type") match {
      case JsDefined(PAY_PAL_TAG) => payPalJsonFormat.reads(json)
      case JsDefined(STRIPE_TAG) => stripeJsonFormat.reads(json)
      case unknown => JsError(__ \ "type", ValidationError(s"Invalid BankDetails value ${unknown}"))
    }

    override def writes(o: PaymentRequest): JsValue = o match {
      case pp: BraintreePaymentRequest => payPalJsonFormat.writes(pp) + ("type" -> PAY_PAL_TAG)
      case s: StripePaymentRequest => stripeJsonFormat.writes(s) + ("type" -> STRIPE_TAG)
    }
  }

}
