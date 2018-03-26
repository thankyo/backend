package com.clemble.loveit.payment.service

import com.clemble.loveit.user.service.repository.UserRepository
import org.junit.runner.RunWith
import org.specs2.concurrent.ExecutionEnv
import org.specs2.runner.JUnitRunner

@RunWith(classOf[JUnitRunner])
class PendingTransactionServiceSpec(implicit ee: ExecutionEnv) extends PaymentServiceTestExecutor {

  val thankTransService = dependency[PendingTransactionService]
  val userRepo = dependency[UserRepository]

  "PAYMENT" should {

    "same resource allows multiple transactions" in {
      val giver = createUser()
      val res = randomResource

      val A = createProject()

      thank(giver, A, res)
      thank(giver, A, res)

      val payments = pendingCharges(giver)
      payments.size must beEqualTo(2)
    }

    "list all transactions" in {
      val giver = createUser()

      val A = createProject()
      val B = createProject()

      val transactionA = thank(giver, A, randomResource)
      val transactionB = thank(giver, B, randomResource)

      val payments = pendingCharges(giver)
      payments must containAllOf(Seq(transactionA, transactionB))
    }

    "remove transactions" in {
      val giver = createUser()

      val A = createProject()
      val B = createProject()

      val transactionA = thank(giver, A, randomResource)
      val transactionB = thank(giver, B, randomResource)

      await(thankTransService.removeCharges(giver, Seq(transactionB)))

      val payments = pendingCharges(giver)
      payments must containAllOf(Seq(transactionA))
    }

  }

}
