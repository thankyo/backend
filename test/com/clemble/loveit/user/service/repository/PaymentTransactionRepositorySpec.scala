package com.clemble.loveit.user.service.repository

import com.clemble.loveit.payment.service.repository.PaymentTransactionRepository
import com.clemble.loveit.test.util.PaymentTransactionGenerator


class PaymentTransactionRepositorySpec extends RepositorySpec {

  val repository = application.injector.instanceOf[PaymentTransactionRepository]

  "CREATE" should {

    "simple create" in {
      val payment = PaymentTransactionGenerator.generate()
      val savedPayment = await(repository.save(payment))
      payment must beEqualTo(savedPayment)
    }

  }
}
