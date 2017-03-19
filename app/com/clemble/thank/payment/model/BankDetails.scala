package com.clemble.thank.payment.model

import com.braintreegateway.{Customer, Transaction => BraintreeTransaction}
import com.clemble.thank.model.Email
import play.api.data.validation.ValidationError
import play.api.libs.json._

/**
  * Bank details abstraction
  */
sealed trait BankDetails
case object EmptyBankDetails extends BankDetails
case class PayPalBankDetails(email: Email) extends BankDetails

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
    PayPalBankDetails(customer.getEmail)
  }

  /**
    * JSON format for [[BankDetails]]
    */
  implicit val jsonFormat = new Format[BankDetails] {

    val PAY_PAL = JsString("payPal")
    val EMPTY = JsString("empty")

    override def reads(json: JsValue): JsResult[BankDetails] = (json \ "type") match {
      case JsDefined(PAY_PAL) => PayPalBankDetails.jsonFormat.reads(json)
      case JsDefined(EMPTY) => JsSuccess(EmptyBankDetails)
      case unknown => JsError(__ \ "type", ValidationError(s"Invalid BankDetails value ${unknown}"))
    }

    override def writes(o: BankDetails): JsValue = o match {
      case pp: PayPalBankDetails => PayPalBankDetails.jsonFormat.writes(pp) + ("type" -> PAY_PAL)
      case EmptyBankDetails => JsObject(Seq("type" -> EMPTY))
    }
  }

}