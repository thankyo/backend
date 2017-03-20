package com.clemble.thank.service.repository

import com.clemble.thank.model.User
import com.clemble.thank.model.error.{RepositoryError, RepositoryException}
import com.clemble.thank.payment.model.BankDetails
import com.clemble.thank.test.util.UserGenerator
import org.apache.commons.lang3.RandomStringUtils
import org.junit.runner.RunWith
import org.specs2.concurrent.ExecutionEnv
import org.specs2.runner.JUnitRunner

import scala.concurrent.Future
import scala.util.{Failure, Success}

@RunWith(classOf[JUnitRunner])
class UserRepositorySpec(implicit val ee: ExecutionEnv) extends RepositorySpec {

  val userRepo = application.injector.instanceOf[UserRepository]

  "CREATE" should {

    "support single create" in {
      val user = UserGenerator.generate()
      val fCreate: Future[User] = userRepo.save(user)
      fCreate must await(beEqualTo(user))

      val fSearch: Future[Option[User]] = userRepo.findById(user.id)
      fSearch must await(beEqualTo(Some(user)))
    }

    "throw Exception on multiple creation" in {
      val user = UserGenerator.generate()

      val fSecondCreate: Future[User] = for {
        _ <- userRepo.save(user)
        sCreate <- userRepo.save(user)
      } yield {
        sCreate
      }

      val fSecondRes = fSecondCreate.
        map(Success(_)).
        recover({
          case t: Throwable => Failure(t)
        })
      fSecondRes must await(beEqualTo(Failure(new RepositoryException(RepositoryError.duplicateKey()))))
    }

    "throw Exception on same account used more then once creation" in {
      val A = UserGenerator.generate().copy(bankDetails = BankDetails.payPal(RandomStringUtils.random(10)))
      val B = UserGenerator.generate().copy(bankDetails = BankDetails.payPal(RandomStringUtils.random(10)))

      await(userRepo.save(A))
      await(userRepo.save(B))

      await(userRepo.setBankDetails(A.id, B.bankDetails)) must beEqualTo(true)
    }

  }

  "CHANGE balance" should {

    "increase when possitive" in {
      val user = UserGenerator.generate()

      val matchResult = for {
        savedUser <- userRepo.save(user)
        _ <- userRepo.changeBalance(user.id, 10)
        updatedUser <- userRepo.findById(user.id).map(_.get)
      } yield {
        savedUser.balance shouldEqual 0
        updatedUser.balance shouldEqual 10
      }

      matchResult.await
    }

    "decrease when negative" in {
      val user = UserGenerator.generate()

      val matchResult = for {
        savedUser <- userRepo.save(user)
        _ <- userRepo.changeBalance(user.id, -10)
        updatedUser <- userRepo.findById(user.id).map(_.get)
      } yield {
        savedUser.balance shouldEqual 0
        updatedUser.balance shouldEqual -10
      }

      matchResult.await
    }

  }

}
