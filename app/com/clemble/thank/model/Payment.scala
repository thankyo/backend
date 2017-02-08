package com.clemble.thank.model

import org.joda.time.DateTime
import play.api.libs.json.Json

sealed trait PaymentOperation
case object Debit extends PaymentOperation
case object Credit extends PaymentOperation

object PaymentOperation {

  implicit val json = Json.format[PaymentOperation]

}

case class Payment (
                     customer: String,
                     amount: Amount,
                     bankDetails: BankDetails,
                     operation: PaymentOperation,
                     createdDate: DateTime
)

object Payment {

  val json = Json.format[Payment]

}