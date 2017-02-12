package com.clemble.thank.service

import com.clemble.thank.model.{ResourceOwnership, User}
import com.clemble.thank.model.error.{RepositoryError, RepositoryException}
import com.clemble.thank.test.util.UserGenerator
import org.specs2.concurrent.ExecutionEnv
import org.apache.commons.lang3.RandomStringUtils._

import scala.util.{Failure, Success}

class UserServiceSpec(implicit ee: ExecutionEnv) extends ServiceSpec {

  val service = application.injector.instanceOf[UserService]

  "CREATE" should {

    "create user" in {
      val user = UserGenerator.generate()
      val createAndGet = service.create(user).flatMap(_ => service.get(user.id))
      createAndGet must await(beEqualTo(Some(user)))
    }

    "return exception on creating the same" in {
      val user = UserGenerator.generate()
      val createAndCreate = service.create(user).flatMap(_ => service.create(user)).
        map(Success(_)).
        recover({ case t: Throwable => Failure(t)})

      createAndCreate must await(beEqualTo(Failure(new RepositoryException(RepositoryError.duplicateKey()))))
    }

  }

  def createUserWithOwnership(owns: ResourceOwnership): User = {
    val user = UserGenerator.generate(
      owns
    )
    val savedUser = await(service.create(user))
    await(service.get(user.id)).get.balance shouldEqual 0
    savedUser
  }

  "UPDATE OWNER BALANCE single hierarchy" should {
    "UPDATE with FULL control" in {
      val url = s"http/example.com/some/${randomNumeric(10)}"

      val user = createUserWithOwnership(ResourceOwnership.full(url))

      await(service.updateOwnerBalance(url, 99))
      await(service.get(user.id)).get.balance shouldEqual 99
    }

    "UNREALIZED url control" in {
      val url = s"http/example.com/some/${randomNumeric(10)}"

      val user = createUserWithOwnership(ResourceOwnership.unrealized(url))

      await(service.updateOwnerBalance(url, 99))
      await(service.get(user.id)).get.balance shouldEqual 99
    }

    "IGNORE with PARTIAL control" in {
      val url = s"http/example.com/some/${randomNumeric(10)}"

      val user = createUserWithOwnership(ResourceOwnership.partial(url))

      await(service.updateOwnerBalance(url, 99))
      await(service.get(user.id)).get.balance shouldEqual 0
    }
  }

  "OWNER BALANCE with FULL parent" in {
    "FULL url control" in {
      val parentUrl = s"http/example.com/some/${randomNumeric(10)}"
      val url = s"${parentUrl}/${randomNumeric(10)}"

      val parentUser = createUserWithOwnership(ResourceOwnership.full(parentUrl))
      val user = createUserWithOwnership(ResourceOwnership.full(url))

      await(service.updateOwnerBalance(url, 99))
      await(service.get(user.id)).get.balance shouldEqual 99
      await(service.get(parentUser.id)).get.balance shouldEqual 0
    }

    "UNREALIZED url control" in {
      val parentUrl = s"http/example.com/some/${randomNumeric(10)}"
      val url = s"${parentUrl}/${randomNumeric(10)}"

      val parentUser = createUserWithOwnership(ResourceOwnership.full(parentUrl))
      val user = createUserWithOwnership(ResourceOwnership.unrealized(url))

      await(service.updateOwnerBalance(url, 99))
      await(service.get(user.id)).get.balance shouldEqual 99
      await(service.get(parentUser.id)).get.balance shouldEqual 0
    }

    "IGNORE with PARTIAL url control" in {
      val parentUrl = s"http/example.com/some/${randomNumeric(10)}"
      val url = s"${parentUrl}/${randomNumeric(10)}"

      val parentUser = createUserWithOwnership(ResourceOwnership.full(parentUrl))
      val user = createUserWithOwnership(ResourceOwnership.partial(url))

      await(service.updateOwnerBalance(url, 99))
      await(service.get(user.id)).get.balance shouldEqual 0
      await(service.get(parentUser.id)).get.balance shouldEqual 99
    }
  }

  "OWNER BALANCE with UNREALIZED parent" in {

    "FULL url control" in {
      val parentUrl = s"http/example.com/some/${randomNumeric(10)}"
      val url = s"${parentUrl}/${randomNumeric(10)}"

      val parentUser = createUserWithOwnership(ResourceOwnership.unrealized(parentUrl))
      val user = createUserWithOwnership(ResourceOwnership.full(url))

      await(service.updateOwnerBalance(url, 99))
      await(service.get(user.id)).get.balance shouldEqual 99
      await(service.get(parentUser.id)).get.balance shouldEqual 0
    }

    "UNREALIZED url control" in {
      val parentUrl = s"http/example.com/some/${randomNumeric(10)}"
      val url = s"${parentUrl}/${randomNumeric(10)}"

      val parentUser = createUserWithOwnership(ResourceOwnership.unrealized(parentUrl))
      val user = createUserWithOwnership(ResourceOwnership.unrealized(url))

      await(service.updateOwnerBalance(url, 99))
      await(service.get(user.id)).get.balance shouldEqual 99
      await(service.get(parentUser.id)).get.balance shouldEqual 0
    }

    "IGNORE with PARTIAL url control" in {
      val parentUrl = s"http/example.com/some/${randomNumeric(10)}"
      val url = s"${parentUrl}/${randomNumeric(10)}"

      val parentUser = createUserWithOwnership(ResourceOwnership.unrealized(parentUrl))
      val user = createUserWithOwnership(ResourceOwnership.partial(url))

      await(service.updateOwnerBalance(url, 99))
      await(service.get(user.id)).get.balance shouldEqual 0
      await(service.get(parentUser.id)).get.balance shouldEqual 99
    }
  }

  "OWNER BALANCE with PARTIAL parent" in {
    "FULL url control" in {
      val parentUrl = s"http/example.com/some/${randomNumeric(10)}"
      val url = s"${parentUrl}/${randomNumeric(10)}"

      val parentUser = createUserWithOwnership(ResourceOwnership.partial(parentUrl))
      val user = createUserWithOwnership(ResourceOwnership.full(url))

      await(service.updateOwnerBalance(url, 99))
      await(service.get(user.id)).get.balance shouldEqual 99
      await(service.get(parentUser.id)).get.balance shouldEqual 0
    }

    "UNREALIZED url control" in {
      val parentUrl = s"http/example.com/some/${randomNumeric(10)}"
      val url = s"${parentUrl}/${randomNumeric(10)}"

      val parentUser = createUserWithOwnership(ResourceOwnership.partial(parentUrl))
      val user = createUserWithOwnership(ResourceOwnership.unrealized(url))

      await(service.updateOwnerBalance(url, 99))
      await(service.get(user.id)).get.balance shouldEqual 99
      await(service.get(parentUser.id)).get.balance shouldEqual 0
    }

    "IGNORE with PARTIAL control" in {
      val parentUrl = s"http/example.com/some/${randomNumeric(10)}"
      val url = s"${parentUrl}/${randomNumeric(10)}"

      val parentUser = createUserWithOwnership(ResourceOwnership.partial(parentUrl))
      val user = createUserWithOwnership(ResourceOwnership.partial(url))

      await(service.updateOwnerBalance(url, 99))
      await(service.get(user.id)).get.balance shouldEqual 0
      await(service.get(parentUser.id)).get.balance shouldEqual 0
    }
  }
}
