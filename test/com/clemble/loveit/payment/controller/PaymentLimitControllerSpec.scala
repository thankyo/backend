package com.clemble.loveit.payment.controller

import com.clemble.loveit.payment.model.Money
import com.clemble.loveit.payment.service.repository.PaymentLimitScenario
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner

@RunWith(classOf[JUnitRunner])
class PaymentLimitControllerSpec extends PaymentControllerTestExecutor with PaymentLimitScenario {

}
