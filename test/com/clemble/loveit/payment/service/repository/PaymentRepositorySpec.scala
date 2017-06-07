package com.clemble.loveit.payment.service.repository

import com.clemble.loveit.common.RepositorySpec
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner

@RunWith(classOf[JUnitRunner])
class PaymentRepositorySpec extends RepositorySpec {

  val repo = dependency[PaymentRepository]

  "list" in {
    val A = createUser()
    val B = createUser()

    val bankDetails = repo.listBankDetails().toSeq()
    bankDetails must containAllOf(Seq(A.id -> A.bankDetails, B.id -> B.bankDetails))
  }

}
