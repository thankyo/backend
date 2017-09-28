package com.clemble.loveit.thank.service

import com.clemble.loveit.common.error.ResourceException
import com.clemble.loveit.common.model.{Amount, HttpResource, Resource, UserID}
import com.clemble.loveit.payment.service.PaymentServiceTestExecutor
import com.clemble.loveit.thank.service.repository.ThankRepository
import com.clemble.loveit.user.model.User
import org.apache.commons.lang3.RandomStringUtils._
import org.junit.runner.RunWith
import org.specs2.concurrent.ExecutionEnv
import org.specs2.runner.JUnitRunner

@RunWith(classOf[JUnitRunner])
class ThankServiceSpec(implicit val ee: ExecutionEnv) extends PaymentServiceTestExecutor {

  val thankService = dependency[ThankService]
  val thankRepo = dependency[ThankRepository]
  val supportedProjectService = dependency[UserSupportedProjectsService]
  val roService = dependency[ROService]

  def createScene():(Resource, User, User) = {
    val url = HttpResource(s"example.com/some/${randomNumeric(10)}")
    // TODO flow must be changed here to use ResourceOwnersip verification
    val owner = someUser()
    await(roService.assignOwnership(owner.id, url))
    await(thankRepo.updateOwner(owner.id, url))
    val giver = someUser()

    (url, owner, giver)
  }

  def thank(user: UserID, url: Resource) = {
    await(thankService.thank(user, url))
  }

  def getSupported(user: UserID) = {
    await(supportedProjectService.getSupported(user));
  }

  def getBalance(url: Resource): Amount = {
    await(thankService.getOrCreate(url)).given
  }

  "thanked" should {

    "return false on random res" in {
      val user = someRandom[UserID]
      val res = someRandom[Resource]

      await(thankService.hasThanked(user, res)) should throwA[ResourceException]
    }

    "return false on not thanked res" in {
      val (res, _, giver) = createScene()

      await(thankService.hasThanked(giver.id, res)) shouldEqual false
    }

    "return true if thanked" in {
      val (res, _, giver) = createScene()

      await(thankService.thank(giver.id, res))
      await(thankService.hasThanked(giver.id, res)) shouldEqual true
    }

  }


  "Thank " should {

    "Decrement for the giver" in {
      val (url, _, giver) = createScene()

      thank(giver.id, url)
      eventually(getBalance(url) shouldEqual 1)

      eventually(- 1 shouldEqual getBalance(giver.id))
    }

    "Increment for the owner" in {
      val (url, owner, giver) = createScene()

      thank(giver.id, url)
      eventually(getBalance(url) shouldEqual 1)

      eventually(1 shouldEqual getBalance(owner.id))
    }

    "Double thank has no effect" in {
      val (url, owner, giver) = createScene()

      getBalance(owner.id) shouldEqual 0
      getBalance(giver.id) shouldEqual 0

      // Double thank has no effect
      thank(giver.id, url)
      thank(giver.id, url)
      thank(giver.id, url)
      eventually(getBalance(url) shouldEqual 1)

      // Balance did not change
      eventually(getBalance(owner.id) shouldEqual 1)
      eventually(getBalance(giver.id) shouldEqual - 1)
    }

  }

  "Supported projects " should {

    "be initialized on Thank" in {
      val (url, owner, giver) = createScene()

      getSupported(giver.id) shouldEqual Nil
      getSupported(owner.id) shouldEqual Nil

      thank(giver.id, url)

      getSupported(owner.id) shouldEqual Nil
      eventually(getSupported(giver.id) shouldEqual List(owner))
    }

  }

}
