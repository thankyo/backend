package com.clemble.loveit.payment.model

import com.clemble.loveit.common.util.WriteableUtils
import play.api.data.validation.ValidationError
import play.api.libs.json._

/**
  * Simple abstraction over payout account
  */
trait PayoutAccount

/**
  * Stripe account credentials
  */
case class StripePayoutAccount(accountId: String, refreshToken: String, accessToken: String) extends PayoutAccount

object PayoutAccount {

  /**
    * JSON format for [[StripePayoutAccount]]
    */
  private val stripeJsonFormat = Json.format[StripePayoutAccount]

  /**
    * JSON format for [[PayoutAccount]]
    */
  implicit val jsonFormat = new Format[PayoutAccount] {

    val STRIPE_TAG = JsString("stripe")

    override def reads(json: JsValue): JsResult[PayoutAccount] = (json \ "type") match {
      case JsDefined(STRIPE_TAG) => stripeJsonFormat.reads(json)
      case unknown => JsError(__ \ "type", ValidationError(s"Invalid ChargeAccount value ${unknown}"))
    }

    override def writes(o: PayoutAccount): JsValue = o match {
      case s: StripePayoutAccount => stripeJsonFormat.writes(s) + ("type" -> STRIPE_TAG)
    }
  }

  implicit val chargeAccountWriteable = WriteableUtils.jsonToWriteable[PayoutAccount]

}
