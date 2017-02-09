package com.clemble.thank.test.util

import com.clemble.thank.model.{BankDetails, PayPalBankDetails}
import org.apache.commons.lang3.RandomStringUtils._

object BankDetailsGenerator extends Generator[BankDetails] {

  override def generate(): BankDetails = {
    PayPalBankDetails(randomNumeric(10))
  }

}
