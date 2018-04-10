package com.clemble.loveit.thank.service

import com.clemble.loveit.common.error.{PaymentException, ResourceException, SelfLovingForbiddenException}
import com.clemble.loveit.common.model.{Amount, OpenGraphObject, Post, Resource, UserID}
import com.clemble.loveit.payment.service.PaymentServiceTestExecutor
import org.junit.runner.RunWith
import org.specs2.concurrent.ExecutionEnv
import org.specs2.runner.JUnitRunner

@RunWith(classOf[JUnitRunner])
class PostServiceSpec(implicit val ee: ExecutionEnv) extends PaymentServiceTestExecutor {

  def createScene():(Resource, UserID, UserID) = {
    val owner = createUser()
    val giver = createUser()

    val url = s"https://example.com/some/${someRandom[Long]}"

    createProject(owner, url)
    await(postService.create(someRandom[OpenGraphObject].copy(url = url)))

    (url, owner, giver)
  }

  def thank(user: UserID, url: Resource): Post = {
    await(postService.thank(user, url))
  }

  def getBalance(url: Resource): Amount = {
    await(postService.getPostOrProject(url)) match {
      case Left(post) => post.thank.given
      case _ => 0
    }
  }

  "thanked" should {

    "return throw Exception on random res" in {
      val user = someRandom[UserID]
      val res = randomResource

      await(postService.hasSupported(user, res)) should throwA[ResourceException]
      await(postService.hasSupported(user, res)) should throwA[ResourceException]
    }

    "return false on not thanked res" in {
      val (res, _, giver) = createScene()

      await(postService.hasSupported(giver, res)) shouldEqual false
    }

    "return true if thanked" in {
      val (res, _, giver) = createScene()

      thank(giver, res)
      await(postService.hasSupported(giver, res)) shouldEqual true
    }

    "can't thank one self" in {
      val project = createProject()

      thank(project.user, project.url) should throwA[SelfLovingForbiddenException]
    }

  }

  "UPDATE OWNER" should {

    "create if missing" in {
      val owner = createUser()
      val url = randomResource

      await(postService.getPostOrProject(url)) should throwA()

      createProject(owner, url).url shouldEqual url
      await(postService.getPostOrProject(url)).right.exists(_.user == owner) should beTrue
    }

    "forbid second creation" in {
      val url = randomResource

      val A = createUser()

      createProject(A, url).url shouldEqual url
      await(postService.getPostOrProject(url)).isRight shouldEqual true
      await(postService.getPostOrProject(url)).right.exists(_.user == A) should beTrue

      val B = createUser()

      createProject(B, url) should throwA[ResourceException]
    }

  }

}
