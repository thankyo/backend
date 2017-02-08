package com.clemble.thank.model

import play.api.libs.json.{Format, JsResult, JsValue, Json}

/**
  * Bank details abstraction
  */
sealed trait BankDetails

case class PayPalBankDetails (email: Email)

object PayPalBankDetails {

  implicit val json = Json.format[PayPalBankDetails]

}

object BankDetails {

  implicit val format = new Format[BankDetails] {
    override def reads(json: JsValue): JsResult[BankDetails] = ???

    override def writes(o: BankDetails): JsValue = ???
  }

}