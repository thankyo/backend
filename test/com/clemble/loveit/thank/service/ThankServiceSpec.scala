package com.clemble.loveit.thank.service

import com.clemble.loveit.common.ServiceSpec
import com.clemble.loveit.common.model.{Amount, HttpResource, Resource, UserID}
import com.clemble.loveit.test.util.UserGenerator
import com.clemble.loveit.thank.model.{Thank}
import com.clemble.loveit.thank.service.repository.ThankRepository
import com.clemble.loveit.user.model.User
import com.clemble.loveit.user.service.repository.UserRepository
import org.apache.commons.lang3.RandomStringUtils._
import org.junit.runner.RunWith
import org.specs2.concurrent.ExecutionEnv
import org.specs2.runner.JUnitRunner

import scala.util.Try

@RunWith(classOf[JUnitRunner])
class ThankServiceSpec(implicit val ee: ExecutionEnv) extends ServiceSpec {

  val thankService = dependency[ThankService]
  val thankRepo = dependency[ThankRepository]
  val userRepo = dependency[UserRepository]

  def createScene():(Resource, User, User) = {
    val url = HttpResource(s"example.com/some/${randomNumeric(10)}")
    // TODO flow must be changed here to use ResourceOwnersip verification
    val owner = await(userRepo.save(UserGenerator.generate().assignOwnership(url)))
    await(thankRepo.save(Thank(url, owner.id)))
    val giver = await(userRepo.save(UserGenerator.generate()))


    (url, owner, giver)
  }

  def thank(user: UserID, url: Resource) = {
    await(thankService.thank(user, url))
  }

  def getBalance(user: String): Amount = {
    await(userRepo.findById(user).map(_.get)).balance
  }

  def getBalance(url: Resource): Amount = {
    await(thankService.getOrCreate(url)).given
  }

  "Thank " should {


    "Decrement for the giver" in {
      val (url, _, giver) = createScene()

      thank(giver.id, url)
      getBalance(url) shouldEqual 1

      val giverBalanceAfterThank = getBalance(giver.id)
      giver.balance - 1 shouldEqual giverBalanceAfterThank
    }

    "Increment for the owner" in {
      val (url, owner, giver) = createScene()

      thank(giver.id, url)
      getBalance(url) shouldEqual 1

      val ownerBalanceAfterThank = getBalance(owner.id)
      (owner.balance + 1) shouldEqual ownerBalanceAfterThank
    }

    "Double thank neutralizes effect" in {
      val (url, owner, giver) = createScene()

      // Double thank has no effect
      thank(giver.id, url)
      Try{ thank(giver.id, url) }
      getBalance(url) shouldEqual 1

      // Balance did not change
      getBalance(owner.id) shouldEqual owner.balance + 1
      getBalance(giver.id) shouldEqual giver.balance - 1
    }

  }

}
