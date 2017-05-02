package com.clemble.loveit.payment.model

import com.braintreegateway.{Customer, PayPalDetails}
import com.clemble.loveit.common.model.Email
import play.api.data.validation.ValidationError
import play.api.libs.json._

/**
  * Bank details abstraction
  */
sealed trait BankDetails
case object EmptyBankDetails extends BankDetails
case class PayPalBankDetails(email: Email) extends BankDetails {
  require(email != null)
}
case class StripeBankDetails(customer: String) extends BankDetails {
  require(customer != null)
}

object BankDetails {

  val empty: BankDetails = EmptyBankDetails

  def payPal(email: String): BankDetails = PayPalBankDetails(email)

  def stripe(customer: String): StripeBankDetails = StripeBankDetails(customer)

  def from(customer: Customer) = payPal(customer.getEmail)

  def from(payPalDetails: PayPalDetails) = payPal(payPalDetails.getPayerEmail())

  /**
    * JSON format for [[PayPalBankDetails]]
    */
  private val payPalJsonFormat = Json.format[PayPalBankDetails]
  /**
    * JSON format for [[StripeBankDetails]]
    */
  private val stripeJsonFormat = Json.format[StripeBankDetails]

  /**
    * JSON format for [[BankDetails]]
    */
  implicit val jsonFormat = new Format[BankDetails] {

    val PAY_PAL_TAG = JsString("payPal")
    val STRIPE_TAG = JsString("stripe")
    val EMPTY_TAG = JsString("empty")

    override def reads(json: JsValue): JsResult[BankDetails] = (json \ "type") match {
      case JsDefined(PAY_PAL_TAG) => payPalJsonFormat.reads(json)
      case JsDefined(STRIPE_TAG) => stripeJsonFormat.reads(json)
      case JsDefined(EMPTY_TAG) => JsSuccess(empty)
      case unknown => JsError(__ \ "type", ValidationError(s"Invalid BankDetails value ${unknown}"))
    }

    override def writes(o: BankDetails): JsValue = o match {
      case pp: PayPalBankDetails => payPalJsonFormat.writes(pp) + ("type" -> PAY_PAL_TAG)
      case s: StripeBankDetails => stripeJsonFormat.writes(s) + ("type" -> STRIPE_TAG)
      case EmptyBankDetails => JsObject(Seq("type" -> EMPTY_TAG))
    }
  }

}