package com.clemble.thank.service.repository

import com.clemble.thank.model.User
import com.clemble.thank.model.error.{RepositoryError, RepositoryException}
import com.clemble.thank.test.util.UserGenerator
import org.junit.runner.RunWith
import org.specs2.concurrent.ExecutionEnv
import org.specs2.runner.JUnitRunner

import scala.concurrent.Future
import scala.util.{Failure, Success}

@RunWith(classOf[JUnitRunner])
class UserRepositorySpec(implicit val ee: ExecutionEnv) extends RepositorySpec {

  val userRepository = application.injector.instanceOf[UserRepository]

  "CREATE" should {

    "support single create" in {
      val user = UserGenerator.generate()
      val fCreate: Future[User] = userRepository.save(user)
      fCreate must await(beEqualTo(user))

      val fSearch: Future[Option[User]] = userRepository.findById(user.id)
      fSearch must await(beEqualTo(Some(user)))
    }

    "throw Exception on multiple creation" in {
      val user = UserGenerator.generate()

      val fSecondCreate: Future[User] = for {
        _ <- userRepository.save(user)
        sCreate <- userRepository.save(user)
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

  }

  "CHANGE balance" should {

    "increase when possitive" in {
      val user = UserGenerator.generate()

      val matchResult = for {
        savedUser <- userRepository.save(user)
        _ <- userRepository.changeBalance(user.id, 10)
        updatedUser <- userRepository.findById(user.id).map(_.get)
      } yield {
        savedUser.balance shouldEqual 0
        updatedUser.balance shouldEqual 10
      }

      matchResult.await
    }

    "decrease when negative" in {
      val user = UserGenerator.generate()

      val matchResult = for {
        savedUser <- userRepository.save(user)
        _ <- userRepository.changeBalance(user.id, -10)
        updatedUser <- userRepository.findById(user.id).map(_.get)
      } yield {
        savedUser.balance shouldEqual 0
        updatedUser.balance shouldEqual -10
      }

      matchResult.await
    }

  }

}
