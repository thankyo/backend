package com.clemble.thank.model

import play.api.data.validation.ValidationError
import play.api.libs.json._

/**
  * Bank details abstraction
  */
sealed trait BankDetails

case class PayPalBankDetails (email: Email) extends BankDetails

object PayPalBankDetails {

  implicit val json = Json.format[PayPalBankDetails]

}

object BankDetails {

  implicit val format = new Format[BankDetails] {

    val PAY_PAL = JsString("payPal")

    override def reads(json: JsValue): JsResult[BankDetails] = json \ "type" match {
      case PAY_PAL => PayPalBankDetails.json.reads(json)
      case unknown => JsError(__ \ "type", ValidationError(s"Invalid BankDetails value ${unknown}"))
    }

    override def writes(o: BankDetails): JsValue = o match {
      case pp : PayPalBankDetails => PayPalBankDetails.json.writes(pp) + ("type" -> PAY_PAL)
    }
  }

}