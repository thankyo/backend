package com.clemble.loveit.payment.controller

import com.clemble.loveit.payment.model.Money
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner

@RunWith(classOf[JUnitRunner])
class MonthlyLimitControllerSpec extends PaymentControllerTestExecutor {

  "Update Limit" in {
    val user = createUser()
    val limit = someRandom[Money]

    val limitBefore = getMonthlyLimit(user)
    val updatedLimit = Some(setMonthlyLimit(user, limit))

    val limitAfter = getMonthlyLimit(user)

    limitAfter shouldEqual updatedLimit
    limitBefore shouldNotEqual limitAfter
  }



}
