package com.clemble.thank.service

import akka.stream.scaladsl.Sink
import com.clemble.thank.model.{Payment, ResourceOwnership}
import com.clemble.thank.service.repository.UserRepository
import com.clemble.thank.test.util.UserGenerator
import org.apache.commons.lang3.RandomStringUtils
import org.junit.runner.RunWith
import org.specs2.concurrent.ExecutionEnv
import org.specs2.runner.JUnitRunner

@RunWith(classOf[JUnitRunner])
class UserPaymentServiceSpec(implicit ee: ExecutionEnv) extends ServiceSpec {

  val paymentService = application.injector.instanceOf[UserPaymentService]
  val userRepo = application.injector.instanceOf[UserRepository]

  "PAYMENT" should {

    "Debit increases User balance" in {
      val user = UserGenerator.generate().copy(balance = 100)

      val savedUser = await(userRepo.save(user))
      await(paymentService.operation(user.id, "example.com", 100))
      val readUser = await(userRepo.findById(user.id).map(_.get))

      savedUser.balance shouldEqual 100
      readUser.balance shouldEqual 0
    }

    "Credit decrease User balance" in {
      val url = s"http://${RandomStringUtils.randomNumeric(100)}.com"
      val user = UserGenerator.generate().copy(balance = 100)

      val savedUser = await(userRepo.save(user))
      await(paymentService.operation(user.id, url, 10))
      val readUser = await(userRepo.findById(user.id).map(_.get))

      savedUser.balance shouldEqual 100
      readUser.balance shouldEqual 90
    }

    "list all transactions" in {
      val user = UserGenerator.generate()

      await(userRepo.save(user))
      val A = await(paymentService.operation(user.id, RandomStringUtils.randomNumeric(100), 100))
      val B = await(paymentService.operation(user.id, RandomStringUtils.randomNumeric(100), 10))
      val payments = await(paymentService.payments(user.id).runWith(Sink.seq[Payment]))

      val expected = (A ++ B).filter(_.user == user.id)
      payments must containAllOf(expected)
    }

  }

}
