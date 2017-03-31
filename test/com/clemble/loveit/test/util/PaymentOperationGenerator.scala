package com.clemble.loveit.test.util

import com.clemble.loveit.payment.model.{Credit, Debit, PaymentOperation}
import org.apache.commons.lang3.RandomUtils._

object PaymentOperationGenerator extends Generator[PaymentOperation] {

  override def generate(): PaymentOperation = {
    if (nextInt(0, 1) == 0)
      Debit
    else
      Credit
  }

}
