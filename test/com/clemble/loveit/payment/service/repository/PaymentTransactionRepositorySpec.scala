package com.clemble.loveit.payment.service.repository

import com.clemble.loveit.common.RepositorySpec
import com.clemble.loveit.payment.model.PaymentTransaction

class PaymentTransactionRepositorySpec extends RepositorySpec {

  val repository = dependency[PaymentTransactionRepository]

  "CREATE" should {

    "simple create" in {
      val payment = someRandom[PaymentTransaction]
      val savedPayment = await(repository.save(payment))
      payment must beEqualTo(savedPayment)
    }

  }
}
