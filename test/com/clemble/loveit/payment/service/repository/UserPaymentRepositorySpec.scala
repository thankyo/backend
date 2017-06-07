package com.clemble.loveit.payment.service.repository

import com.clemble.loveit.common.RepositorySpec
import com.clemble.loveit.payment.model.UserPayment
import com.clemble.loveit.user.model.User
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner

@RunWith(classOf[JUnitRunner])
class UserPaymentRepositorySpec extends RepositorySpec {

  val repo = dependency[UserPaymentRepository]

  "find all" in {
    val A = createUser()
    val B = createUser()

    val payments = repo.find().toSeq()
    val expected = Seq(A, B).map(view[User, UserPayment](_))
    payments must containAllOf(expected)
  }

}
