package com.clemble.loveit.payment.model

import com.clemble.loveit.common.model.UserID
import com.clemble.loveit.common.util.LoveItCurrency
import play.api.libs.json.Json

trait PaymentUser {
  val id: UserID
  val bankDetails: BankDetails
  val monthlyLimit: Money
}

object PaymentUser {

  val DEFAULT_LIMIT = Money(BigDecimal(10), LoveItCurrency.getInstance("USD"))

}

case class SimplePaymentUser(
                            id: UserID,
                            bankDetails: BankDetails,
                            monthlyLimit: Money
)

object SimplePaymentUser {

  implicit val jsonFormat = Json.format[SimplePaymentUser]

}
