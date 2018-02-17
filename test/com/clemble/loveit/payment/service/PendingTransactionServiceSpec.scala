package com.clemble.loveit.payment.service

import com.clemble.loveit.common.model.Resource
import com.clemble.loveit.user.service.repository.UserRepository
import org.junit.runner.RunWith
import org.specs2.concurrent.ExecutionEnv
import org.specs2.runner.JUnitRunner

import scala.util.Try

@RunWith(classOf[JUnitRunner])
class PendingTransactionServiceSpec(implicit ee: ExecutionEnv) extends PaymentServiceTestExecutor {

  val thankTransService = dependency[PendingTransactionService]
  val userRepo = dependency[UserRepository]

  "PAYMENT" should {

    "same resource transactions ignored" in {
      val giver = createUser()
      val res = someRandom[Resource]

      val A = createProject()

      thank(giver, A, res)
      Try(thank(giver, A, res))

      val payments = outgoingTransactions(giver)
      payments.size must beEqualTo(1)
    }

    "list all transactions" in {
      val giver = createUser()

      val A = createProject()
      val B = createProject()

      val transactionA = thank(giver, A, someRandom[Resource])
      val transactionB = thank(giver, B, someRandom[Resource])

      val payments = outgoingTransactions(giver)
      payments must containAllOf(Seq(transactionA, transactionB))
    }

    "remove transactions" in {
      val giver = createUser()

      val A = createProject()
      val B = createProject()

      val transactionA = thank(giver, A, someRandom[Resource])
      val transactionB = thank(giver, B, someRandom[Resource])

      await(thankTransService.removeOutgoing(giver, Seq(transactionB)))

      val payments = outgoingTransactions(giver)
      payments must containAllOf(Seq(transactionA))
    }

  }

}
