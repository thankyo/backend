package com.clemble.loveit.test.util

import com.clemble.loveit.model.{ThankTransaction, User}
import org.apache.commons.lang3.RandomUtils._

object ThankTransactionGenerator extends Generator[ThankTransaction] {

  override def generate(): ThankTransaction = {
    if (nextInt(0, 1) == 0)
      ThankTransaction.debit(UserGenerator.generate().id, ResourceGenerator.generate(), nextLong(0, Long.MaxValue))
    else
      ThankTransaction.credit(UserGenerator.generate().id, ResourceGenerator.generate(), nextLong(0, Long.MaxValue))
  }

  def generate(user: User): ThankTransaction = {
    if (nextInt(0, 1) == 0)
      ThankTransaction.debit(user.id, ResourceGenerator.generate(), nextLong(0, Long.MaxValue))
    else
      ThankTransaction.credit(user.id, ResourceGenerator.generate(),  nextLong(0, Long.MaxValue))
  }

}
