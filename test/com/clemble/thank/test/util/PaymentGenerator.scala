package com.clemble.thank.test.util

import com.clemble.thank.model.{Payment, User, UserId}
import org.apache.commons.lang3.RandomUtils._

object PaymentGenerator extends Generator[Payment]{

  override def generate(): Payment = {
    if (nextInt(0, 1) == 0)
      Payment.debit(UserGenerator.generate(), nextLong(0, Long.MaxValue))
    else
      Payment.credit(UserGenerator.generate(), nextLong(0, Long.MaxValue))
  }

  def generate(user: User): Payment = {
    if (nextInt(0, 1) == 0)
      Payment.debit(user, nextLong(0, Long.MaxValue))
    else
      Payment.credit(user, nextLong(0, Long.MaxValue))
  }

}
