package com.clemble.loveit.payment.model

import com.clemble.loveit.common.model.{Amount, UserID}
import com.clemble.loveit.common.util.LoveItCurrency

trait UserPayment {
  val id: UserID
  val balance: Amount
  val bankDetails: BankDetails
  val monthlyLimit: Money
}

object UserPayment {

  val DEFAULT_LIMIT = Money(BigDecimal(10), LoveItCurrency.getInstance("USD"))

}