package com.clemble.thank.service

import com.clemble.thank.test.util.UserGenerator
import org.specs2.concurrent.ExecutionEnv

import org.apache.commons.lang3.RandomStringUtils._

class ThankServiceSpec(implicit val ee: ExecutionEnv) extends ServiceSpec {

  val thankService = application.injector.instanceOf[ThankService]
  val userService = application.injector.instanceOf[UserService]

  "Thank " should {

    "Decrement for the giver" in {
      val url = s"http/example.com/some/${randomNumeric(10)}"

      val giver = UserGenerator.generate()
      val matchResult = for {
        beforeThank <- userService.create(giver)
        thank <- thankService.thank(giver.id, url)
        afterThank <- userService.get(giver.id).map(_.get)
      } yield {
        thank.given shouldEqual 1
        beforeThank.balance - 1 shouldEqual afterThank.balance
      }
      matchResult.await
    }

    "Increment for the owner" in {
      val url = s"http/example.com/some/${randomNumeric(10)}"
      val owner = UserGenerator.generate().copy(owns = List(url))
      val giver = UserGenerator.generate()
      val matchResult = for {
        _ <- userService.create(giver)
        beforeThank <- userService.create(owner)
        thank <- thankService.thank(giver.id, url)
        afterThank <- userService.get(owner.id).map(_.get)
      } yield {
        thank.given shouldEqual 1
        (beforeThank.balance + 1) shouldEqual afterThank.balance
      }
      matchResult.await
    }

  }

}
