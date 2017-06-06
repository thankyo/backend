package com.clemble.loveit.user.service

import com.clemble.loveit.common.ServiceSpec
import com.clemble.loveit.common.error.{RepositoryException}
import com.clemble.loveit.common.model.{HttpResource, Resource}
import com.clemble.loveit.user.model.User
import com.clemble.loveit.payment.service.ThankTransactionService
import com.clemble.loveit.user.service.repository.UserRepository
import org.apache.commons.lang3.RandomStringUtils._
import org.junit.runner.RunWith
import org.specs2.concurrent.ExecutionEnv
import org.specs2.runner.JUnitRunner

import scala.util.{Failure, Success}

@RunWith(classOf[JUnitRunner])
class UserServiceSpec(implicit ee: ExecutionEnv) extends ServiceSpec {

  val service = dependency[UserService]
  val repo = dependency[UserRepository]
  val transactionService = dependency[ThankTransactionService]

  val giver = someRandom[User].copy(balance = Int.MaxValue)
  await(repo.save(giver))

  "CREATE" should {

    "create user" in {
      val user = someRandom[User]
      val createAndGet = repo.save(user).flatMap(_ => repo.findById(user.id))
      createAndGet must await(beEqualTo(Some(user)))
    }

    "return exception on creating the same" in {
      val user = someRandom[User]
      val createAndCreate = repo.save(user).flatMap(_ => repo.save(user)).flatMap(_ => repo.save(user))

      await(createAndCreate) should throwA[RepositoryException]
    }

  }

  def createUserWithOwnership(owns: Resource): User = {
    val user = someRandom[User].
      assignOwnership(owns).
      copy(balance = 0)
    val savedUser = await(repo.save(user))
    await(repo.findById(user.id)).get.balance shouldEqual 0
    savedUser
  }

  "UPDATE OWNER BALANCE single hierarchy" should {
    "UPDATE with FULL control" in {
      val url = HttpResource(s"example.com/some/${randomNumeric(10)}")

      val user = createUserWithOwnership(url)

      await(transactionService.create(giver.id, user.id, url))
      await(repo.findById(user.id)).get.balance shouldEqual 1
    }

    "UNREALIZED url control" in {
      val url = HttpResource(s"example.com/some/${randomNumeric(10)}")

      val user = createUserWithOwnership(url)

      await(transactionService.create(giver.id, user.id, url))
      await(repo.findById(user.id)).get.balance shouldEqual 1
    }

    "IGNORE with PARTIAL control" in {
      val url = HttpResource(s"example.com/some/${randomNumeric(10)}")

      val user = createUserWithOwnership(url)

      await(transactionService.create(giver.id, user.id, url))
      await(repo.findById(user.id)).get.balance shouldEqual 1
    }
  }

  "OWNER BALANCE with FULL parent" in {
    "FULL url control" in {
      val parentUrl = HttpResource(s"example.com/some/${randomNumeric(10)}")
      val url = HttpResource(s"${parentUrl.uri}/${randomNumeric(10)}")

      val parentUser = createUserWithOwnership(parentUrl)
      val user = createUserWithOwnership(url)

      await(transactionService.create(giver.id, user.id, url))
      await(repo.findById(user.id)).get.balance shouldEqual 1
      await(repo.findById(parentUser.id)).get.balance shouldEqual 0
    }

    "UNREALIZED url control" in {
      val parentUrl = HttpResource(s"example.com/some/${randomNumeric(10)}")
      val url = HttpResource(s"${parentUrl.uri}/${randomNumeric(10)}")

      val parentUser = createUserWithOwnership(parentUrl)
      val user = createUserWithOwnership(url)

      await(transactionService.create(giver.id, user.id, url))
      await(repo.findById(user.id)).get.balance shouldEqual 1
      await(repo.findById(parentUser.id)).get.balance shouldEqual 0
    }

    "IGNORE with PARTIAL url control" in {
      val parentUrl = HttpResource(s"example.com/some/${randomNumeric(10)}")
      val url = HttpResource(s"${parentUrl.uri}/${randomNumeric(10)}")

      val parentUser = createUserWithOwnership(parentUrl)
      val user = createUserWithOwnership(url)

      await(transactionService.create(giver.id, user.id, url))
      await(repo.findById(user.id)).get.balance shouldEqual 1
      await(repo.findById(parentUser.id)).get.balance shouldEqual 0
    }
  }

  "OWNER BALANCE with UNREALIZED parent" in {

    "FULL url control" in {
      val parentUrl = HttpResource(s"example.com/some/${randomNumeric(10)}")
      val url = HttpResource(s"${parentUrl.uri}/${randomNumeric(10)}")

      val parentUser = createUserWithOwnership(parentUrl)
      val user = createUserWithOwnership(url)

      await(transactionService.create(giver.id, user.id, url))
      await(repo.findById(user.id)).get.balance shouldEqual 1
      await(repo.findById(parentUser.id)).get.balance shouldEqual 0
    }

    "UNREALIZED url control" in {
      val parentUrl = HttpResource(s"example.com/some/${randomNumeric(10)}")
      val url = HttpResource(s"${parentUrl.uri}/${randomNumeric(10)}")

      val parentUser = createUserWithOwnership(parentUrl)
      val user = createUserWithOwnership(url)

      await(transactionService.create(giver.id, user.id, url))
      await(repo.findById(user.id)).get.balance shouldEqual 1
      await(repo.findById(parentUser.id)).get.balance shouldEqual 0
    }

    "PARTIAL url control" in {
      val parentUrl = HttpResource(s"example.com/some/${randomNumeric(10)}")
      val url = HttpResource(s"${parentUrl.uri}/${randomNumeric(10)}")

      val parentUser = createUserWithOwnership(parentUrl)
      val user = createUserWithOwnership(url)

      await(transactionService.create(giver.id, user.id, url))
      await(repo.findById(user.id)).get.balance shouldEqual 1
      await(repo.findById(parentUser.id)).get.balance shouldEqual 0
    }
  }

  "OWNER BALANCE with PARTIAL parent" in {
    "FULL url control" in {
      val parentUrl = HttpResource(s"example.com/some/${randomNumeric(10)}")
      val url = HttpResource(s"${parentUrl.uri}/${randomNumeric(10)}")

      val parentUser = createUserWithOwnership(parentUrl)
      val user = createUserWithOwnership(url)

      await(transactionService.create(giver.id, user.id, url))
      await(repo.findById(user.id)).get.balance shouldEqual 1
      await(repo.findById(parentUser.id)).get.balance shouldEqual 0
    }

    "UNREALIZED url control" in {
      val parentUrl = HttpResource(s"example.com/some/${randomNumeric(10)}")
      val url = HttpResource(s"${parentUrl.uri}/${randomNumeric(10)}")

      val parentUser = createUserWithOwnership(parentUrl)
      val user = createUserWithOwnership(url)

      await(transactionService.create(giver.id, user.id, url))
      await(repo.findById(user.id)).get.balance shouldEqual 1
      await(repo.findById(parentUser.id)).get.balance shouldEqual 0
    }

    "IGNORE with PARTIAL control" in {
      val parentUrl = HttpResource(s"example.com/some/${randomNumeric(10)}")
      val url = HttpResource(s"${parentUrl.uri}/${randomNumeric(10)}")

      val parentUser = createUserWithOwnership(parentUrl)
      val user = createUserWithOwnership(url)

      await(transactionService.create(giver.id, user.id, url))
      await(repo.findById(user.id)).get.balance shouldEqual 1
      await(repo.findById(parentUser.id)).get.balance shouldEqual 0
    }
  }
}
