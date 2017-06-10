package com.clemble.loveit.payment.model

import com.clemble.loveit.common.util.{LoveItCurrency, WriteableUtils}
import play.api.data.validation.ValidationError
import play.api.libs.json._

/**
  * Bank details abstraction
  */
sealed trait BankDetails {
  def fee: Money
  def minCharge: Money
}

case class StripeBankDetails(customer: String, brand: Option[String] = None, last4: Option[String] = None) extends BankDetails {
  require(customer != null)
  val fee = StripeBankDetails.STRIPE_FEE
  val minCharge = StripeBankDetails.STRIPE_MIN_CHARGE
}

object StripeBankDetails {

  private val STRIPE_FEE = Money(0.3, LoveItCurrency.getInstance("USD"))
  private val STRIPE_MIN_CHARGE = Money(1.0, LoveItCurrency.getInstance("USD"))

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