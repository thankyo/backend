package com.clemble.thank.test.util

import com.clemble.thank.payment.model.BankDetails
import org.apache.commons.lang3.RandomStringUtils._
import org.apache.commons.lang3.RandomUtils._

object BankDetailsGenerator extends Generator[BankDetails] {

  override def generate(): BankDetails = {
    if (nextInt(0, 1) == 0)
      BankDetails.payPal(randomNumeric(10))
    else
      BankDetails.empty
  }

}
