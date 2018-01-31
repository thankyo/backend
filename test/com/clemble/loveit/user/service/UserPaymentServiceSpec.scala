package com.clemble.loveit.user.service

import com.clemble.loveit.common.model.{HttpResource, Resource, UserID}
import com.clemble.loveit.payment.service.{PaymentServiceTestExecutor, PendingTransactionService}
import com.clemble.loveit.thank.model.SupportedProject
import com.clemble.loveit.thank.service.repository.SupportedProjectRepository
import org.junit.runner.RunWith
import org.specs2.concurrent.ExecutionEnv
import org.specs2.runner.JUnitRunner

@RunWith(classOf[JUnitRunner])
class UserPaymentServiceSpec(implicit ee: ExecutionEnv) extends PaymentServiceTestExecutor {

  val service = dependency[UserService]
  val roRepo = dependency[SupportedProjectRepository]
  val transactionService = dependency[PendingTransactionService]

  val giver = createUser()

  def createUserWithOwnership(res: Resource): SupportedProject = {
    val user = createUser()
    val project = SupportedProject(res, user)
    await(roRepo.saveProject(project)) shouldEqual true
    project
  }

  "UPDATE OWNER BALANCE single hierarchy" should {
    "UPDATE with FULL control" in {
      val url = HttpResource(s"example.com/some/${someRandom[Long]}")

      val project = createUserWithOwnership(url)

      thank(giver, project, url)
      getBalance(project.user) shouldEqual 1
    }

    "UNREALIZED url control" in {
      val url = HttpResource(s"example.com/some/${someRandom[Long]}")

      val project = createUserWithOwnership(url)

      thank(giver, project, url)
      getBalance(project.user) shouldEqual 1
    }

    "IGNORE with PARTIAL control" in {
      val url = HttpResource(s"example.com/some/${someRandom[Long]}")

      val project = createUserWithOwnership(url)

      thank(giver, project, url)
      getBalance(project.user) shouldEqual 1
    }
  }

  "OWNER BALANCE with FULL parent" in {
    "FULL url control" in {
      val parentUrl = HttpResource(s"example.com/some/${someRandom[Long]}")
      val url = HttpResource(s"${parentUrl.uri}/${someRandom[Long]}")

      val parentProject = createUserWithOwnership(parentUrl)
      val project = createUserWithOwnership(url)

      thank(giver, project, url)
      getBalance(project.user) shouldEqual 1
      getBalance(parentProject.user) shouldEqual 0
    }

    "UNREALIZED url control" in {
      val parentUrl = HttpResource(s"example.com/some/${someRandom[Long]}")
      val url = HttpResource(s"${parentUrl.uri}/${someRandom[Long]}")

      val parentProject = createUserWithOwnership(parentUrl)
      val project = createUserWithOwnership(url)

      thank(giver, project, url)
      getBalance(project.user) shouldEqual 1
      getBalance(parentProject.user) shouldEqual 0
    }

    "IGNORE with PARTIAL url control" in {
      val parentUrl = HttpResource(s"example.com/some/${someRandom[Long]}")
      val url = HttpResource(s"${parentUrl.uri}/${someRandom[Long]}")

      val parentProject = createUserWithOwnership(parentUrl)
      val project = createUserWithOwnership(url)

      thank(giver, project, url)
      getBalance(project.user) shouldEqual 1
      getBalance(parentProject.user) shouldEqual 0
    }
  }

  "OWNER BALANCE with UNREALIZED parent" in {

    "FULL url control" in {
      val parentUrl = HttpResource(s"example.com/some/${someRandom[Long]}")
      val url = HttpResource(s"${parentUrl.uri}/${someRandom[Long]}")

      val parentProject = createUserWithOwnership(parentUrl)
      val project = createUserWithOwnership(url)

      thank(giver, project, url)
      getBalance(project.user) shouldEqual 1
      getBalance(parentProject.user) shouldEqual 0
    }

    "UNREALIZED url control" in {
      val parentUrl = HttpResource(s"example.com/some/${someRandom[Long]}")
      val url = HttpResource(s"${parentUrl.uri}/${someRandom[Long]}")

      val parentProject = createUserWithOwnership(parentUrl)
      val project = createUserWithOwnership(url)

      thank(giver, project, url)
      getBalance(project.user) shouldEqual 1
      getBalance(parentProject.user) shouldEqual 0
    }

    "PARTIAL url control" in {
      val parentUrl = HttpResource(s"example.com/some/${someRandom[Long]}")
      val url = HttpResource(s"${parentUrl.uri}/${someRandom[Long]}")

      val parentProject = createUserWithOwnership(parentUrl)
      val project = createUserWithOwnership(url)

      thank(giver, project, url)
      getBalance(project.user) shouldEqual 1
      getBalance(parentProject.user) shouldEqual 0
    }
  }

  "OWNER BALANCE with PARTIAL parent" in {
    "FULL url control" in {
      val parentUrl = HttpResource(s"example.com/some/${someRandom[Long]}")
      val url = HttpResource(s"${parentUrl.uri}/${someRandom[Long]}")

      val parentProject = createUserWithOwnership(parentUrl)
      val project = createUserWithOwnership(url)

      thank(giver, project, url)
      getBalance(project.user) shouldEqual 1
      getBalance(parentProject.user) shouldEqual 0
    }

    "UNREALIZED url control" in {
      val parentUrl = HttpResource(s"example.com/some/${someRandom[Long]}")
      val url = HttpResource(s"${parentUrl.uri}/${someRandom[Long]}")

      val parentProject = createUserWithOwnership(parentUrl)
      val project = createUserWithOwnership(url)

      thank(giver, project, url)
      getBalance(project.user) shouldEqual 1
      getBalance(parentProject.user) shouldEqual 0
    }

    "IGNORE with PARTIAL control" in {
      val parentUrl = HttpResource(s"example.com/some/${someRandom[Long]}")
      val url = HttpResource(s"${parentUrl.uri}/${someRandom[Long]}")

      val parentProject = createUserWithOwnership(parentUrl)
      val project = createUserWithOwnership(url)

      thank(giver, project, url)
      getBalance(project.user) shouldEqual 1
      getBalance(parentProject.user) shouldEqual 0
    }
  }
}
