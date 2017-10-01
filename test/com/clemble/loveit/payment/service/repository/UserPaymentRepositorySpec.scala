package com.clemble.loveit.payment.service.repository

import com.clemble.loveit.common.RepositorySpec
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner

@RunWith(classOf[JUnitRunner])
class UserPaymentRepositorySpec extends RepositorySpec {

  val repo = dependency[PaymentRepository]

  "find all" in {
    val A = createUser()
    val B = createUser()

    val payments = repo.find().toSeq().map(_._id)
    val expected = Seq(A, B)
    payments must containAllOf(expected)
  }

}
