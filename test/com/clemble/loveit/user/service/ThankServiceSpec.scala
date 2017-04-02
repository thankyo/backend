package com.clemble.loveit.user.service

import com.clemble.loveit.common.model.HttpResource
import com.clemble.loveit.user.service.repository.UserRepository
import com.clemble.loveit.test.util.UserGenerator
import com.clemble.loveit.thank.model.ResourceOwnership
import com.clemble.loveit.thank.service.ThankService
import org.apache.commons.lang3.RandomStringUtils._
import org.junit.runner.RunWith
import org.specs2.concurrent.ExecutionEnv
import org.specs2.runner.JUnitRunner

@RunWith(classOf[JUnitRunner])
class ThankServiceSpec(implicit val ee: ExecutionEnv) extends ServiceSpec {

  val thankService = application.injector.instanceOf[ThankService]
  val userRepo = application.injector.instanceOf[UserRepository]

  "Thank " should {

    "Decrement for the giver" in {
      val url = HttpResource(s"example.com/some/${randomNumeric(10)}")

      val giver = UserGenerator.generate()

      val beforeThank = await(userRepo.save(giver))
      val thank = await(thankService.thank(giver.id, url))
      val afterThank = await(userRepo.findById(giver.id).map(_.get))
      thank.given shouldEqual 1
      beforeThank.balance - 1 shouldEqual afterThank.balance
    }

    "Increment for the owner" in {
      val url = HttpResource(s"example.com/some/${randomNumeric(10)}")
      val owner = UserGenerator.generate().assignOwnership(0, ResourceOwnership.full(url))
      val giver = UserGenerator.generate()

      await(userRepo.save(giver))
      val beforeThank = await(userRepo.save(owner))
      val thank = await(thankService.thank(giver.id, url))
      val afterThank = await(userRepo.findById(owner.id).map(_.get))

      thank.given shouldEqual 1
      (beforeThank.balance + 1) shouldEqual afterThank.balance
    }

  }

}
