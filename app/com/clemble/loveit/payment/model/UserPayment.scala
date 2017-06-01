package com.clemble.loveit.payment.model

import com.clemble.loveit.common.model.UserID
import com.clemble.loveit.common.util.LoveItCurrency
import play.api.libs.json.Json

trait UserPayment {
  val id: UserID
  val bankDetails: BankDetails
  val monthlyLimit: Money
}

object UserPayment {

  val DEFAULT_LIMIT = Money(BigDecimal(10), LoveItCurrency.getInstance("USD"))

}