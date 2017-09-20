package com.clemble.loveit.payment.model

import com.clemble.loveit.common.util.{WriteableUtils}
import play.api.libs.json._

/**
  * Bank details abstraction
  */
sealed trait ChargeAccount extends PaymentAccount

case class StripeChargeAccount(customer: String, brand: Option[String] = None, last4: Option[String] = None) extends ChargeAccount {
  require(customer != null && customer.length != 0)
}

object ChargeAccount {

  val DEFAULT = StripeChargeAccount("undefined", brand = Some("Visa"), last4 = Some("0000"))

  /**
    * JSON format for [[StripeChargeAccount]]
    */
  private val stripeJsonFormat = Json.format[StripeChargeAccount]

  /**
    * JSON format for [[ChargeAccount]]
    */
  implicit val jsonFormat = new Format[ChargeAccount] {

    val STRIPE_TAG = JsString("stripe")

    override def reads(json: JsValue): JsResult[ChargeAccount] = (json \ "type") match {
      case JsDefined(STRIPE_TAG) => stripeJsonFormat.reads(json)
      case unknown => JsError(__ \ "type", JsonValidationError(s"Invalid ChargeAccount value ${unknown}"))
    }

    override def writes(o: ChargeAccount): JsValue = o match {
      case s: StripeChargeAccount => stripeJsonFormat.writes(s) + ("type" -> STRIPE_TAG)
    }
  }

  implicit val chargeAccountWriteable = WriteableUtils.jsonToWriteable[ChargeAccount]

}