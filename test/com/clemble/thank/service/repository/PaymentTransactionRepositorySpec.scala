package com.clemble.thank.service.repository

import com.clemble.thank.test.util.{PaymentTransactionGenerator}


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
