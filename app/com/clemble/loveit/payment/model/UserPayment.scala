package com.clemble.loveit.payment.model

import com.clemble.loveit.common.model.{Amount, UserID}
import com.clemble.loveit.common.util.LoveItCurrency

/**
  * User view for Payments
  */
trait UserPayment {
  /**
    * User identifier
    */
  val id: UserID
  /**
    * Current balance which can be negative
    */
  val balance: Amount
  /**
    * BankDetails to use for withdraw and
    */
  val bankDetails: BankDetails
  /**
    * Monthly transaction limit
    */
  val monthlyLimit: Money
  /**
    * Pending transactions
    */
  val pending: List[ThankTransaction]
}

object UserPayment {

  val DEFAULT_LIMIT = Money(BigDecimal(10), LoveItCurrency.getInstance("USD"))

}