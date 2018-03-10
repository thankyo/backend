package com.clemble.loveit.thank.service

import com.clemble.loveit.common.model.{Resource, UserID}
import com.clemble.loveit.payment.service.PaymentServiceTestExecutor
import com.clemble.loveit.thank.model.{OpenGraphObject, Project}
import com.clemble.loveit.thank.service.repository.PostRepository
import org.junit.runner.RunWith
import org.specs2.concurrent.ExecutionEnv
import org.specs2.runner.JUnitRunner

@RunWith(classOf[JUnitRunner])
class ProjectServiceSpec(implicit val ee: ExecutionEnv) extends PaymentServiceTestExecutor {

  val thankRepo = dependency[PostRepository]
  val trackService = dependency[ProjectSupportTrackService]

  def createScene():(Project, UserID, UserID) = {
    val owner = createUser()
    val giver = createUser()

    val url = s"https://example.com/some/${someRandom[Long]}"

    val project = createProject(owner, url)
    await(postService.create(someRandom[OpenGraphObject].copy(url = url)))

    (project, owner, giver)
  }

  def thank(user: UserID, url: Resource) = {
    await(postService.thank(user, url))
  }

  def getSupported(user: UserID) = {
    await(trackService.getSupported(user))
  }

  "Supported projects " should {

    "be initialized on Thank" in {
      val (project, owner, giver) = createScene()

      getSupported(giver) shouldEqual Nil
      getSupported(owner) shouldEqual Nil

      thank(giver, project.url)

      getSupported(owner) shouldEqual Nil
      eventually(getSupported(giver) shouldEqual List(project))
    }

  }

}
