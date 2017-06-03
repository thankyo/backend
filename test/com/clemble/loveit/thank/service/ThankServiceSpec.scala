package com.clemble.loveit.thank.service

import com.clemble.loveit.common.ServiceSpec
import com.clemble.loveit.common.error.PaymentException
import com.clemble.loveit.common.model.{Amount, HttpResource, Resource, UserID}
import com.clemble.loveit.thank.service.repository.ThankRepository
import com.clemble.loveit.user.model.User
import com.clemble.loveit.user.service.repository.UserRepository
import org.apache.commons.lang3.RandomStringUtils._
import org.junit.runner.RunWith
import org.specs2.concurrent.ExecutionEnv
import org.specs2.runner.JUnitRunner

@RunWith(classOf[JUnitRunner])
class ThankServiceSpec(implicit val ee: ExecutionEnv) extends ServiceSpec {

  val thankService = dependency[ThankService]
  val thankRepo = dependency[ThankRepository]
  val userRepo = dependency[UserRepository]

  def createScene():(Resource, User, User) = {
    val url = HttpResource(s"example.com/some/${randomNumeric(10)}")
    // TODO flow must be changed here to use ResourceOwnersip verification
    val owner = await(userRepo.save(someRandom[User].assignOwnership(url)))
    await(thankRepo.updateOwner(owner.id, url))
    val giver = await(userRepo.save(someRandom[User]))


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

      eventually(owner.balance + 1 shouldEqual getBalance(owner.id))
    }

    "Double thank has no effect" in {
      val (url, owner, giver) = createScene()

      getBalance(owner.id) shouldEqual owner.balance
      getBalance(giver.id) shouldEqual giver.balance

      // Double thank has no effect
      thank(giver.id, url)
      thank(giver.id, url) should throwA[PaymentException]
      getBalance(url) shouldEqual 1

      // Balance did not change
      eventually(getBalance(owner.id) shouldEqual owner.balance + 1)
      eventually(getBalance(giver.id) shouldEqual giver.balance - 1)
    }

  }

}
