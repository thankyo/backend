package com.clemble.thank.model

import org.joda.time.DateTime
import play.api.libs.json.Json

case class PaymentTransaction(
                               id: String,
                               operation: PaymentOperation,
                               user: UserID,
                               thanks: Amount,
                               money: Money,
                               source: BankDetails,
                               destination: BankDetails,
                               created: DateTime
                             ) extends Transaction

object PaymentTransaction {

  implicit val jsonFormat = Json.format[PaymentTransaction]

}


