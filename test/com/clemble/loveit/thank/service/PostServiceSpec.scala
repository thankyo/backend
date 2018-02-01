package com.clemble.loveit.thank.service

import com.clemble.loveit.common.model.{Amount, Resource, UserID}
import com.clemble.loveit.payment.service.PaymentServiceTestExecutor
import com.clemble.loveit.thank.model.{OpenGraphObject, SupportedProject}
import com.clemble.loveit.thank.service.repository.PostRepository
import org.junit.runner.RunWith
import org.specs2.concurrent.ExecutionEnv
import org.specs2.runner.JUnitRunner

@RunWith(classOf[JUnitRunner])
class PostServiceSpec(implicit val ee: ExecutionEnv) extends PaymentServiceTestExecutor {

  val service = dependency[PostService]
  val repo = dependency[PostRepository]
  val supportedProjectService = dependency[SupportedProjectService]

  def createScene():(Resource, UserID, UserID) = {
    val owner = createUser()
    val giver = createUser()

    val url = s"https://example.com/some/${someRandom[Long]}"
    val resource = Resource.from(url)

    await(roService.validate(SupportedProject(resource, owner)))
    await(service.create(someRandom[OpenGraphObject].copy(url = url)))

    (resource, owner, giver)
  }

  def thank(user: UserID, url: Resource) = {
    await(service.thank(user, url))
  }

  def getBalance(url: Resource): Amount = {
    await(service.getPostOrProject(url)) match {
      case Left(post) => post.thank.given
      case _ => 0
    }
  }

  "thanked" should {

    "return false on random res" in {
      val user = someRandom[UserID]
      val res = someRandom[Resource]

      await(service.hasSupported(user, res)) shouldEqual false
    }

    "return false on not thanked res" in {
      val (res, _, giver) = createScene()

      await(service.hasSupported(giver, res)) shouldEqual false
    }

    "return true if thanked" in {
      val (res, _, giver) = createScene()

      await(service.thank(giver, res))
      await(service.hasSupported(giver, res)) shouldEqual true
    }

  }


  "Thank " should {

    "Decrement for the giver" in {
      val (url, _, giver) = createScene()

      thank(giver, url)
      eventually(getBalance(url) shouldEqual 1)

      eventually(getBalance(giver) shouldEqual -1)
    }

    "Increment for the owner" in {
      val (url, owner, giver) = createScene()

      thank(giver, url)
      eventually(getBalance(url) shouldEqual 1)

      eventually(getBalance(owner) shouldEqual 1)
    }

    "Double thank has no effect" in {
      val (url, owner, giver) = createScene()

      getBalance(owner) shouldEqual 0
      getBalance(giver) shouldEqual 0

      // Double thank has no effect
      thank(giver, url)
      thank(giver, url)
      thank(giver, url)
      eventually(getBalance(url) shouldEqual 1)

      // Balance did not change
      eventually(getBalance(owner) shouldEqual 1)
      eventually(getBalance(giver) shouldEqual - 1)
    }

  }

  "UPDATE OWNER" should {

    "create if missing" in {
      val owner = createUser()
      val resource = someRandom[Resource]

      await(service.getPostOrProject(resource)) should throwA()

      await(roService.validate(SupportedProject(resource, owner))).resource shouldEqual resource
      await(service.getPostOrProject(resource)).right.exists(_.user == owner) should beTrue
    }

    "update if exists" in {
      val resource = someRandom[Resource]

      val A = createUser()

      await(roService.validate(SupportedProject(resource, A))).resource shouldEqual resource
      await(service.getPostOrProject(resource)).isRight shouldEqual true
      await(service.getPostOrProject(resource)).right.exists(_.user == A) should beTrue

      val B = createUser()

      await(roService.validate(SupportedProject(resource, B))).resource shouldEqual resource
      await(service.getPostOrProject(resource)).isRight shouldEqual true
      await(service.getPostOrProject(resource)).right.exists(_.user == B) should beTrue
    }

  }

}
