package com.clemble.loveit.test.util

import java.util.Currency

import com.clemble.loveit.payment.model.{Money, PaymentTransaction}
import com.clemble.loveit.util.{IDGenerator, LoveItCurrency}

import scala.util.Random

object PaymentTransactionGenerator extends Generator[PaymentTransaction] {

  override def generate(): PaymentTransaction = {
    if (Random.nextBoolean()) {
      PaymentTransaction.debit(IDGenerator.generate(), UserGenerator.generate().id, Random.nextLong(), Money(Random.nextLong(), LoveItCurrency.getInstance("USD")), BankDetailsGenerator.generate())
    } else {
      PaymentTransaction.credit(UserGenerator.generate().id, Random.nextLong(), Money(Random.nextLong(), LoveItCurrency.getInstance("USD")), BankDetailsGenerator.generate())
    }
  }

}
