package com.clemble.loveit.payment.service

import com.clemble.loveit.common.model.Resource
import com.clemble.loveit.payment.service.repository.UserBalanceRepository
import com.clemble.loveit.user.service.repository.UserRepository
import org.junit.runner.RunWith
import org.specs2.concurrent.ExecutionEnv
import org.specs2.runner.JUnitRunner

import scala.util.Try

@RunWith(classOf[JUnitRunner])
class ThankTransactionServiceSpec(implicit ee: ExecutionEnv) extends PaymentServiceTestExecutor {

  val thankTransService = dependency[ThankTransactionService]
  val userRepo = dependency[UserRepository]
  val balanceRepo = dependency[UserBalanceRepository]

  "PAYMENT" should {

    "same resource transactions ignored" in {
      val giver = createUser()
      val res = someRandom[Resource]

      thank(giver, "A", res)
      Try(thank(giver, "B", res))

      val payments = pendingThanks(giver)
      payments.size must beEqualTo(1)

      val giverBalanceAfter = await(balanceRepo.getBalance(giver))
      giverBalanceAfter shouldEqual -1
    }

    "increase resource owner balance" in {
      val giver = createUser()
      val owner = createUser()

      thank(giver, owner, someRandom[Resource])
      val ownerBalanceAfter = await(balanceRepo.getBalance(owner))

      ownerBalanceAfter shouldEqual 1
    }

    "decrease giver balance" in {
      val giver = createUser()

      thank(giver, "B", someRandom[Resource])
      val giverBalanceAfter = await(balanceRepo.getBalance(giver))

      giverBalanceAfter shouldEqual -1
    }

    "list all transactions" in {
      val giver = createUser()

      val transactionA = thank(giver, "A", someRandom[Resource])
      val transactionB = thank(giver, "B", someRandom[Resource])
      val payments = pendingThanks(giver)

      payments must containAllOf(Seq(transactionA, transactionB))
    }

    "remove transactions" in {
      val giver = createUser()

      val transactionA = thank(giver, "A", someRandom[Resource])
      val transactionB = thank(giver, "B", someRandom[Resource])
      await(thankTransService.removeAll(Seq(transactionB)))

      val payments = pendingThanks(giver)

      payments must containAllOf(Seq(transactionA))


    }

  }

}
