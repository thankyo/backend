package com.clemble.thank.service

import com.clemble.thank.model.error.{RepositoryError, RepositoryException}
import com.clemble.thank.model.{HttpResource, ResourceOwnership, User}
import com.clemble.thank.service.repository.UserRepository
import com.clemble.thank.test.util.UserGenerator
import org.apache.commons.lang3.RandomStringUtils._
import org.specs2.concurrent.ExecutionEnv

import scala.util.{Failure, Success}

class UserServiceSpec(implicit ee: ExecutionEnv) extends ServiceSpec {

  val service = application.injector.instanceOf[UserService]
  val repo = application.injector.instanceOf[UserRepository]
  val paymentService = application.injector.instanceOf[ThankTransactionService]

  val giver = UserGenerator.generate()
  await(repo.save(giver))

  "CREATE" should {

    "create user" in {
      val user = UserGenerator.generate()
      val createAndGet = repo.save(user).flatMap(_ => repo.findById(user.id))
      createAndGet must await(beEqualTo(Some(user)))
    }

    "return exception on creating the same" in {
      val user = UserGenerator.generate()
      val createAndCreate = repo.save(user).flatMap(_ => repo.save(user)).
        map(Success(_)).
        recover({ case t: Throwable => Failure(t) })

      createAndCreate must await(beEqualTo(Failure(new RepositoryException(RepositoryError.duplicateKey()))))
    }

  }

  def createUserWithOwnership(owns: ResourceOwnership): User = {
    val user = UserGenerator.generate(
      owns
    )
    val savedUser = await(repo.save(user))
    await(repo.findById(user.id)).get.balance shouldEqual 0
    savedUser
  }

  "UPDATE OWNER BALANCE single hierarchy" should {
    "UPDATE with FULL control" in {
      val url = HttpResource(s"example.com/some/${randomNumeric(10)}")

      val user = createUserWithOwnership(ResourceOwnership.full(url))

      await(paymentService.create(giver.id, url, 99))
      await(repo.findById(user.id)).get.balance shouldEqual 99
    }

    "UNREALIZED url control" in {
      val url = HttpResource(s"example.com/some/${randomNumeric(10)}")

      val user = createUserWithOwnership(ResourceOwnership.unrealized(url))

      await(paymentService.create(giver.id, url, 99))
      await(repo.findById(user.id)).get.balance shouldEqual 99
    }

    "IGNORE with PARTIAL control" in {
      val url = HttpResource(s"example.com/some/${randomNumeric(10)}")

      val user = createUserWithOwnership(ResourceOwnership.partial(url))

      await(paymentService.create(giver.id, url, 99))
      await(repo.findById(user.id)).get.balance shouldEqual 99
    }
  }

  "OWNER BALANCE with FULL parent" in {
    "FULL url control" in {
      val parentUrl = HttpResource(s"example.com/some/${randomNumeric(10)}")
      val url = HttpResource(s"${parentUrl.uri}/${randomNumeric(10)}")

      val parentUser = createUserWithOwnership(ResourceOwnership.full(parentUrl))
      val user = createUserWithOwnership(ResourceOwnership.full(url))

      await(paymentService.create(giver.id, url, 99))
      await(repo.findById(user.id)).get.balance shouldEqual 99
      await(repo.findById(parentUser.id)).get.balance shouldEqual 0
    }

    "UNREALIZED url control" in {
      val parentUrl = HttpResource(s"example.com/some/${randomNumeric(10)}")
      val url = HttpResource(s"${parentUrl.uri}/${randomNumeric(10)}")

      val parentUser = createUserWithOwnership(ResourceOwnership.full(parentUrl))
      val user = createUserWithOwnership(ResourceOwnership.unrealized(url))

      await(paymentService.create(giver.id, url, 99))
      await(repo.findById(user.id)).get.balance shouldEqual 99
      await(repo.findById(parentUser.id)).get.balance shouldEqual 0
    }

    "IGNORE with PARTIAL url control" in {
      val parentUrl = HttpResource(s"example.com/some/${randomNumeric(10)}")
      val url = HttpResource(s"${parentUrl.uri}/${randomNumeric(10)}")

      val parentUser = createUserWithOwnership(ResourceOwnership.full(parentUrl))
      val user = createUserWithOwnership(ResourceOwnership.partial(url))

      await(paymentService.create(giver.id, url, 99))
      await(repo.findById(user.id)).get.balance shouldEqual 99
      await(repo.findById(parentUser.id)).get.balance shouldEqual 0
    }
  }

  "OWNER BALANCE with UNREALIZED parent" in {

    "FULL url control" in {
      val parentUrl = HttpResource(s"example.com/some/${randomNumeric(10)}")
      val url = HttpResource(s"${parentUrl.uri}/${randomNumeric(10)}")

      val parentUser = createUserWithOwnership(ResourceOwnership.unrealized(parentUrl))
      val user = createUserWithOwnership(ResourceOwnership.full(url))

      await(paymentService.create(giver.id, url, 99))
      await(repo.findById(user.id)).get.balance shouldEqual 99
      await(repo.findById(parentUser.id)).get.balance shouldEqual 0
    }

    "UNREALIZED url control" in {
      val parentUrl = HttpResource(s"example.com/some/${randomNumeric(10)}")
      val url = HttpResource(s"${parentUrl.uri}/${randomNumeric(10)}")

      val parentUser = createUserWithOwnership(ResourceOwnership.unrealized(parentUrl))
      val user = createUserWithOwnership(ResourceOwnership.unrealized(url))

      await(paymentService.create(giver.id, url, 99))
      await(repo.findById(user.id)).get.balance shouldEqual 99
      await(repo.findById(parentUser.id)).get.balance shouldEqual 0
    }

    "PARTIAL url control" in {
      val parentUrl = HttpResource(s"example.com/some/${randomNumeric(10)}")
      val url = HttpResource(s"${parentUrl.uri}/${randomNumeric(10)}")

      val parentUser = createUserWithOwnership(ResourceOwnership.unrealized(parentUrl))
      val user = createUserWithOwnership(ResourceOwnership.partial(url))

      await(paymentService.create(giver.id, url, 99))
      await(repo.findById(user.id)).get.balance shouldEqual 99
      await(repo.findById(parentUser.id)).get.balance shouldEqual 0
    }
  }

  "OWNER BALANCE with PARTIAL parent" in {
    "FULL url control" in {
      val parentUrl = HttpResource(s"example.com/some/${randomNumeric(10)}")
      val url = HttpResource(s"${parentUrl.uri}/${randomNumeric(10)}")

      val parentUser = createUserWithOwnership(ResourceOwnership.partial(parentUrl))
      val user = createUserWithOwnership(ResourceOwnership.full(url))

      await(paymentService.create(giver.id, url, 99))
      await(repo.findById(user.id)).get.balance shouldEqual 99
      await(repo.findById(parentUser.id)).get.balance shouldEqual 0
    }

    "UNREALIZED url control" in {
      val parentUrl = HttpResource(s"example.com/some/${randomNumeric(10)}")
      val url = HttpResource(s"${parentUrl.uri}/${randomNumeric(10)}")

      val parentUser = createUserWithOwnership(ResourceOwnership.partial(parentUrl))
      val user = createUserWithOwnership(ResourceOwnership.unrealized(url))

      await(paymentService.create(giver.id, url, 99))
      await(repo.findById(user.id)).get.balance shouldEqual 99
      await(repo.findById(parentUser.id)).get.balance shouldEqual 0
    }

    "IGNORE with PARTIAL control" in {
      val parentUrl = HttpResource(s"example.com/some/${randomNumeric(10)}")
      val url = HttpResource(s"${parentUrl.uri}/${randomNumeric(10)}")

      val parentUser = createUserWithOwnership(ResourceOwnership.partial(parentUrl))
      val user = createUserWithOwnership(ResourceOwnership.partial(url))

      await(paymentService.create(giver.id, url, 99))
      await(repo.findById(user.id)).get.balance shouldEqual 99
      await(repo.findById(parentUser.id)).get.balance shouldEqual 0
    }
  }
}
