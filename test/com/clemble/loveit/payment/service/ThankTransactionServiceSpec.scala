package com.clemble.loveit.payment.service

import akka.stream.scaladsl.Sink
import com.clemble.loveit.common.ServiceSpec
import com.clemble.loveit.common.model.{HttpResource, Resource}
import com.clemble.loveit.payment.model.ThankTransaction
import com.clemble.loveit.user.model.User
import com.clemble.loveit.user.service.repository.UserRepository
import org.apache.commons.lang3.RandomStringUtils
import org.junit.runner.RunWith
import org.specs2.concurrent.ExecutionEnv
import org.specs2.runner.JUnitRunner

@RunWith(classOf[JUnitRunner])
class ThankTransactionServiceSpec(implicit ee: ExecutionEnv) extends ServiceSpec {

  val thankTransService = dependency[ThankTransactionService]
  val userRepo = dependency[UserRepository]

  "PAYMENT" should {

    "Debit increases User balance" in {
      val user = someRandom[User].copy(balance = 100)
      val savedUser = await(userRepo.save(user))

      await(thankTransService.create(user.id, "A", HttpResource("example.com")))
      val readUser = await(userRepo.findById(user.id).map(_.get))

      savedUser.balance shouldEqual 100
      readUser.balance shouldEqual 99
    }

    "Credit decrease User balance" in {
      val url = HttpResource(s"${RandomStringUtils.randomNumeric(100)}.com")
      val user = someRandom[User].copy(balance = 100)
      val savedUser = await(userRepo.save(user))

      await(thankTransService.create(user.id, "B", url))
      val readUser = await(userRepo.findById(user.id).map(_.get))

      savedUser.balance shouldEqual 100
      readUser.balance shouldEqual 99
    }

    "list all transactions" in {
      val giver = createUser()

      val transactionA = await(thankTransService.create(giver, "A", someRandom[Resource]))
      val transactionB = await(thankTransService.create(giver, "B", someRandom[Resource]))
      val payments = await(thankTransService.list(giver).runWith(Sink.seq[ThankTransaction]))

      payments must containAllOf(Seq(transactionA, transactionB))
    }

  }

}
