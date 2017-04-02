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

object PayPalBankDetails {

  /**
    * JSON format for [[PayPalBankDetails]]
    */
  implicit val jsonFormat = Json.format[PayPalBankDetails]

}

object BankDetails {

  val empty: BankDetails = EmptyBankDetails

  def payPal(email: String): BankDetails = PayPalBankDetails(email)

  def from(customer: Customer) = {
    payPal(customer.getEmail)
  }

  def from(payPalDetails: PayPalDetails) = {
    payPal(payPalDetails.getPayerEmail())
  }

  /**
    * JSON format for [[BankDetails]]
    */
  implicit val jsonFormat = new Format[BankDetails] {

    val PAY_PAL = JsString("payPal")
    val EMPTY = JsString("empty")

    override def reads(json: JsValue): JsResult[BankDetails] = (json \ "type") match {
      case JsDefined(PAY_PAL) => PayPalBankDetails.jsonFormat.reads(json)
      case JsDefined(EMPTY) => JsSuccess(empty)
      case unknown => JsError(__ \ "type", ValidationError(s"Invalid BankDetails value ${unknown}"))
    }

    override def writes(o: BankDetails): JsValue = o match {
      case pp: PayPalBankDetails => PayPalBankDetails.jsonFormat.writes(pp) + ("type" -> PAY_PAL)
      case EmptyBankDetails => JsObject(Seq("type" -> EMPTY))
    }
  }

}