package com.clemble.loveit.thank.service

import com.clemble.loveit.common.model.{HttpResource, Resource, UserID}
import com.clemble.loveit.payment.service.PaymentServiceTestExecutor
import com.clemble.loveit.thank.model.{OpenGraphObject, SupportedProject}
import com.clemble.loveit.thank.service.repository.PostRepository
import org.junit.runner.RunWith
import org.specs2.concurrent.ExecutionEnv
import org.specs2.runner.JUnitRunner

@RunWith(classOf[JUnitRunner])
class SupportedProjectServiceSpec(implicit val ee: ExecutionEnv) extends PaymentServiceTestExecutor {

  val postService = dependency[PostService]
  val thankRepo = dependency[PostRepository]
  val supportedProjectService = dependency[SupportedProjectService]

  def createScene():(Resource, UserID, UserID) = {
    val owner = createUser()
    val giver = createUser()

    val url = s"https://example.com/some/${someRandom[Long]}"
    val res = Resource.from(url)

    await(roService.assignOwnership(owner, res))
    await(postService.create(someRandom[OpenGraphObject].copy(url = url)))

    (res, owner, giver)
  }

  def thank(user: UserID, url: Resource) = {
    await(postService.thank(user, url))
  }

  def getSupported(user: UserID) = {
    await(supportedProjectService.getSupported(user))
  }

  "Supported projects " should {

    "be initialized on Thank" in {
      val (url, owner, giver) = createScene()

      getSupported(giver) shouldEqual Nil
      getSupported(owner) shouldEqual Nil

      thank(giver, url)

      getSupported(owner) shouldEqual Nil
      eventually(getSupported(giver) shouldEqual List(SupportedProject from getUser(owner).get))
    }

  }

}
