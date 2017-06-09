package com.clemble.loveit.payment.model

import com.clemble.loveit.common.util.WriteableUtils
import com.clemble.loveit.thank.model.Thank
import play.api.data.validation.ValidationError
import play.api.libs.json._

/**
  * Bank details abstraction
  */
sealed trait BankDetails
case class StripeBankDetails(customer: String, brand: Option[String] = None, last4: Option[String] = None) extends BankDetails {
  require(customer != null)
}

object BankDetails {

  /**
    * JSON format for [[StripeBankDetails]]
    */
  private val stripeJsonFormat = Json.format[StripeBankDetails]

  /**
    * JSON format for [[BankDetails]]
    */
  implicit val jsonFormat = new Format[BankDetails] {

    val STRIPE_TAG = JsString("stripe")

    override def reads(json: JsValue): JsResult[BankDetails] = (json \ "type") match {
      case JsDefined(STRIPE_TAG) => stripeJsonFormat.reads(json)
      case unknown => JsError(__ \ "type", ValidationError(s"Invalid BankDetails value ${unknown}"))
    }

    override def writes(o: BankDetails): JsValue = o match {
      case s: StripeBankDetails => stripeJsonFormat.writes(s) + ("type" -> STRIPE_TAG)
    }
  }

  implicit val bankDetailsWriteable = WriteableUtils.jsonToWriteable[BankDetails]

}