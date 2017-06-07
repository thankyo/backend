package com.clemble.loveit.payment.service

import com.clemble.loveit.common.ServiceSpec
import com.clemble.loveit.common.model.Resource
import com.clemble.loveit.user.service.repository.UserRepository
import org.junit.runner.RunWith
import org.specs2.concurrent.ExecutionEnv
import org.specs2.runner.JUnitRunner

import scala.util.Try

@RunWith(classOf[JUnitRunner])
class ThankTransactionServiceSpec(implicit ee: ExecutionEnv) extends ServiceSpec {

  val thankTransService = dependency[ThankTransactionService]
  val userRepo = dependency[UserRepository]

  "PAYMENT" should {

    "same resource transactions ignored" in {
      val giver = createUser()
      val res = someRandom[Resource]

      await(thankTransService.create(giver, "A", res))
      Try(await(thankTransService.create(giver, "B", res)))

      val payments = thankTransService.list(giver).toSeq()
      payments.size must beEqualTo(1)

      val giverBalanceAfter = await(balanceService.getBalance(giver))
      giverBalanceAfter shouldEqual -1
    }

    "increase resource owner balance" in {
      val giver = createUser()
      val owner = createUser()

      await(thankTransService.create(giver, owner, someRandom[Resource]))
      val ownerBalanceAfter = await(balanceService.getBalance(owner))

      ownerBalanceAfter shouldEqual 1
    }

    "decrease giver balance" in {
      val giver = createUser()

      await(thankTransService.create(giver, "B", someRandom[Resource]))
      val giverBalanceAfter = await(balanceService.getBalance(giver))

      giverBalanceAfter shouldEqual -1
    }

    "list all transactions" in {
      val giver = createUser()

      val transactionA = await(thankTransService.create(giver, "A", someRandom[Resource]))
      val transactionB = await(thankTransService.create(giver, "B", someRandom[Resource]))
      val payments = thankTransService.list(giver).toSeq()

      payments must containAllOf(Seq(transactionA, transactionB))
    }

  }

}
