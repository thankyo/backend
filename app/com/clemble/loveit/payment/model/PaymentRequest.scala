package com.clemble.loveit.payment.model

import play.api.data.validation.ValidationError
import play.api.libs.json._

@Deprecated
sealed trait PaymentRequest {
  val charge: Money
}

@Deprecated
case class StripePaymentRequest(token: String, charge: Money, details: JsObject) extends PaymentRequest

object PaymentRequest {

  val stripeJsonFormat = Json.format[StripePaymentRequest]

  /**
    * JSON format for [[PaymentRequest]]
    */
  implicit val jsonFormat = new Format[PaymentRequest] {

    val STRIPE_TAG = JsString("stripe")

    override def reads(json: JsValue): JsResult[PaymentRequest] = (json \ "type") match {
      case JsDefined(STRIPE_TAG) => stripeJsonFormat.reads(json)
      case unknown => JsError(__ \ "type", ValidationError(s"Invalid BankDetails value ${unknown}"))
    }

    override def writes(o: PaymentRequest): JsValue = o match {
      case s: StripePaymentRequest => stripeJsonFormat.writes(s) + ("type" -> STRIPE_TAG)
    }
  }

}
