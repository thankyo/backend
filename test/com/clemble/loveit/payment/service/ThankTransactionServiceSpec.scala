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
      val user = someRandom[User]

      await(userRepo.save(user))
      val A = await(thankTransService.create(user.id, "A", someRandom[Resource]))
      val B = await(thankTransService.create(user.id, "B", someRandom[Resource]))
      val payments = await(thankTransService.list(user.id).runWith(Sink.seq[ThankTransaction]))

      val expected = (A ++ B).filter(_.user == user.id)
      payments must containAllOf(expected)
    }

  }

}
