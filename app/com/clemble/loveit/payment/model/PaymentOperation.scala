package com.clemble.loveit.payment.model

import play.api.libs.json._

sealed trait PaymentOperation
case object Debit extends PaymentOperation
case object Credit extends PaymentOperation

object PaymentOperation {

  /**
    * JSON format for [[PaymentOperation]]
    */
  implicit val jsonFormat = new Format[PaymentOperation] {

    val DEBIT = JsString("debit")
    val CREDIT = JsString("credit")

    override def reads(json: JsValue): JsResult[PaymentOperation] = json match {
      case DEBIT => JsSuccess(Debit)
      case CREDIT => JsSuccess(Credit)
      case unknown => JsError(s"Invalid PaymentOperation value ${unknown}")
    }

    override def writes(o: PaymentOperation): JsValue = o match {
      case Debit => DEBIT
      case Credit => CREDIT
    }

  }

}