package com.clemble.loveit.payment.model

import java.util.Currency

import com.clemble.loveit.common.SerializationSpec
import com.clemble.loveit.common.model.Money
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner

@RunWith(classOf[JUnitRunner])
class MoneySerializationSpec extends SerializationSpec[Money] {

  "COMPARISON" in {
    val currency = someRandom[Currency]

    Money(100, currency) > Money(99, currency) shouldEqual true
    Money(100, currency) >= Money(99, currency) shouldEqual true
    Money(100, currency) >= Money(100, currency) shouldEqual true
    Money(100, currency) == Money(100, currency) shouldEqual true
    Money(100, currency) <= Money(100, currency) shouldEqual true
    Money(100, currency) <= Money(101, currency) shouldEqual true
    Money(100, currency) < Money(101, currency) shouldEqual true
  }

  "ADDITION" in {
    val currency = someRandom[Currency]
    Money(100, currency) + Money(10, currency) == Money(110, currency) shouldEqual true
  }

}
