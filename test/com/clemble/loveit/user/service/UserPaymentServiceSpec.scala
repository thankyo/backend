package com.clemble.loveit.user.service

import com.clemble.loveit.common.model.{HttpResource, Resource, UserID}
import com.clemble.loveit.payment.service.{PaymentServiceTestExecutor, PendingTransactionService}
import com.clemble.loveit.thank.service.repository.RORepository
import org.junit.runner.RunWith
import org.specs2.concurrent.ExecutionEnv
import org.specs2.runner.JUnitRunner

@RunWith(classOf[JUnitRunner])
class UserPaymentServiceSpec(implicit ee: ExecutionEnv) extends PaymentServiceTestExecutor {

  val service = dependency[UserService]
  val roRepo = dependency[RORepository]
  val transactionService = dependency[PendingTransactionService]

  val giver = createUser()

  def createUserWithOwnership(res: Resource): UserID = {
    val user = createUser()
    await(roRepo.assignOwnership(user, res)) shouldEqual true
    user
  }

  "UPDATE OWNER BALANCE single hierarchy" should {
    "UPDATE with FULL control" in {
      val url = HttpResource(s"example.com/some/${someRandom[Long]}")

      val user = createUserWithOwnership(url)

      thank(giver, user, url)
      getBalance(user) shouldEqual 1
    }

    "UNREALIZED url control" in {
      val url = HttpResource(s"example.com/some/${someRandom[Long]}")

      val user = createUserWithOwnership(url)

      thank(giver, user, url)
      getBalance(user) shouldEqual 1
    }

    "IGNORE with PARTIAL control" in {
      val url = HttpResource(s"example.com/some/${someRandom[Long]}")

      val user = createUserWithOwnership(url)

      thank(giver, user, url)
      getBalance(user) shouldEqual 1
    }
  }

  "OWNER BALANCE with FULL parent" in {
    "FULL url control" in {
      val parentUrl = HttpResource(s"example.com/some/${someRandom[Long]}")
      val url = HttpResource(s"${parentUrl.uri}/${someRandom[Long]}")

      val parentUser = createUserWithOwnership(parentUrl)
      val user = createUserWithOwnership(url)

      thank(giver, user, url)
      getBalance(user) shouldEqual 1
      getBalance(parentUser) shouldEqual 0
    }

    "UNREALIZED url control" in {
      val parentUrl = HttpResource(s"example.com/some/${someRandom[Long]}")
      val url = HttpResource(s"${parentUrl.uri}/${someRandom[Long]}")

      val parentUser = createUserWithOwnership(parentUrl)
      val user = createUserWithOwnership(url)

      thank(giver, user, url)
      getBalance(user) shouldEqual 1
      getBalance(parentUser) shouldEqual 0
    }

    "IGNORE with PARTIAL url control" in {
      val parentUrl = HttpResource(s"example.com/some/${someRandom[Long]}")
      val url = HttpResource(s"${parentUrl.uri}/${someRandom[Long]}")

      val parentUser = createUserWithOwnership(parentUrl)
      val user = createUserWithOwnership(url)

      thank(giver, user, url)
      getBalance(user) shouldEqual 1
      getBalance(parentUser) shouldEqual 0
    }
  }

  "OWNER BALANCE with UNREALIZED parent" in {

    "FULL url control" in {
      val parentUrl = HttpResource(s"example.com/some/${someRandom[Long]}")
      val url = HttpResource(s"${parentUrl.uri}/${someRandom[Long]}")

      val parentUser = createUserWithOwnership(parentUrl)
      val user = createUserWithOwnership(url)

      thank(giver, user, url)
      getBalance(user) shouldEqual 1
      getBalance(parentUser) shouldEqual 0
    }

    "UNREALIZED url control" in {
      val parentUrl = HttpResource(s"example.com/some/${someRandom[Long]}")
      val url = HttpResource(s"${parentUrl.uri}/${someRandom[Long]}")

      val parentUser = createUserWithOwnership(parentUrl)
      val user = createUserWithOwnership(url)

      thank(giver, user, url)
      getBalance(user) shouldEqual 1
      getBalance(parentUser) shouldEqual 0
    }

    "PARTIAL url control" in {
      val parentUrl = HttpResource(s"example.com/some/${someRandom[Long]}")
      val url = HttpResource(s"${parentUrl.uri}/${someRandom[Long]}")

      val parentUser = createUserWithOwnership(parentUrl)
      val user = createUserWithOwnership(url)

      thank(giver, user, url)
      getBalance(user) shouldEqual 1
      getBalance(parentUser) shouldEqual 0
    }
  }

  "OWNER BALANCE with PARTIAL parent" in {
    "FULL url control" in {
      val parentUrl = HttpResource(s"example.com/some/${someRandom[Long]}")
      val url = HttpResource(s"${parentUrl.uri}/${someRandom[Long]}")

      val parentUser = createUserWithOwnership(parentUrl)
      val user = createUserWithOwnership(url)

      thank(giver, user, url)
      getBalance(user) shouldEqual 1
      getBalance(parentUser) shouldEqual 0
    }

    "UNREALIZED url control" in {
      val parentUrl = HttpResource(s"example.com/some/${someRandom[Long]}")
      val url = HttpResource(s"${parentUrl.uri}/${someRandom[Long]}")

      val parentUser = createUserWithOwnership(parentUrl)
      val user = createUserWithOwnership(url)

      thank(giver, user, url)
      getBalance(user) shouldEqual 1
      getBalance(parentUser) shouldEqual 0
    }

    "IGNORE with PARTIAL control" in {
      val parentUrl = HttpResource(s"example.com/some/${someRandom[Long]}")
      val url = HttpResource(s"${parentUrl.uri}/${someRandom[Long]}")

      val parentUser = createUserWithOwnership(parentUrl)
      val user = createUserWithOwnership(url)

      thank(giver, user, url)
      getBalance(user) shouldEqual 1
      getBalance(parentUser) shouldEqual 0
    }
  }
}
