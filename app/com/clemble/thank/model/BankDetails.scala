package com.clemble.thank.model

import play.api.data.validation.ValidationError
import play.api.libs.json._

/**
  * Bank details abstraction
  */
sealed trait BankDetails
case class PayPalBankDetails (email: Email) extends BankDetails

object PayPalBankDetails {

  /**
    * JSON format for [[PayPalBankDetails]]
    */
  implicit val jsonFormat = Json.format[PayPalBankDetails]

}

object BankDetails {

  /**
    * JSON format for [[BankDetails]]
    */
  implicit val jsonFormat = new Format[BankDetails] {

    val PAY_PAL = JsString("payPal")

    override def reads(json: JsValue): JsResult[BankDetails] = (json \ "type") match {
      case JsDefined(PAY_PAL) => PayPalBankDetails.jsonFormat.reads(json)
      case unknown => JsError(__ \ "type", ValidationError(s"Invalid BankDetails value ${unknown}"))
    }

    override def writes(o: BankDetails): JsValue = o match {
      case pp : PayPalBankDetails => PayPalBankDetails.jsonFormat.writes(pp) + ("type" -> PAY_PAL)
    }
  }

}