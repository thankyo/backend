package com.clemble.loveit.user.service

import akka.stream.scaladsl.Sink
import com.clemble.loveit.common.model.{SocialResource, HttpResource}
import com.clemble.loveit.payment.model.ThankTransaction
import com.clemble.loveit.payment.service.ThankTransactionService
import com.clemble.loveit.user.service.repository.UserRepository
import com.clemble.loveit.test.util.UserGenerator
import org.apache.commons.lang3.RandomStringUtils
import org.junit.runner.RunWith
import org.specs2.concurrent.ExecutionEnv
import org.specs2.runner.JUnitRunner

@RunWith(classOf[JUnitRunner])
class ThankTransactionServiceSpec(implicit ee: ExecutionEnv) extends ServiceSpec {

  val thankTransService = application.injector.instanceOf[ThankTransactionService]
  val userRepo = application.injector.instanceOf[UserRepository]

  "PAYMENT" should {

    "Debit increases User balance" in {
      val user = UserGenerator.generate().copy(balance = 100)

      val savedUser = await(userRepo.save(user))
      await(thankTransService.create(user.id, HttpResource("example.com"), 100))
      val readUser = await(userRepo.findById(user.id).map(_.get))

      savedUser.balance shouldEqual 100
      readUser.balance shouldEqual 0
    }

    "Credit decrease User balance" in {
      val url = HttpResource(s"${RandomStringUtils.randomNumeric(100)}.com")
      val user = UserGenerator.generate().copy(balance = 100)

      val savedUser = await(userRepo.save(user))
      await(thankTransService.create(user.id, url, 10))
      val readUser = await(userRepo.findById(user.id).map(_.get))

      savedUser.balance shouldEqual 100
      readUser.balance shouldEqual 90
    }

    "list all transactions" in {
      val user = UserGenerator.generate()

      await(userRepo.save(user))
      val A = await(thankTransService.create(user.id, SocialResource("facebook", RandomStringUtils.randomNumeric(100)), 100))
      val B = await(thankTransService.create(user.id, SocialResource("facebook", RandomStringUtils.randomNumeric(100)), 10))
      val payments = await(thankTransService.list(user.id).runWith(Sink.seq[ThankTransaction]))

      val expected = (A ++ B).filter(_.user == user.id)
      payments must containAllOf(expected)
    }

  }

}
