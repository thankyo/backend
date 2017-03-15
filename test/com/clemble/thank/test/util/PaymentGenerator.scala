package com.clemble.thank.test.util

import com.clemble.thank.model.{Payment, Resource, User}
import org.apache.commons.lang3.RandomStringUtils
import org.apache.commons.lang3.RandomUtils._

object PaymentGenerator extends Generator[Payment] {

  def generateURI(): Resource = RandomStringUtils.randomNumeric(10);

  override def generate(): Payment = {
    if (nextInt(0, 1) == 0)
      Payment.debit(UserGenerator.generate().id, generateURI(), nextLong(0, Long.MaxValue))
    else
      Payment.credit(UserGenerator.generate().id, generateURI(), nextLong(0, Long.MaxValue))
  }

  def generate(user: User): Payment = {
    if (nextInt(0, 1) == 0)
      Payment.debit(user.id, generateURI(), nextLong(0, Long.MaxValue))
    else
      Payment.credit(user.id, generateURI(),  nextLong(0, Long.MaxValue))
  }

}
