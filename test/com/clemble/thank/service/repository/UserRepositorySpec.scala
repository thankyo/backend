package com.clemble.thank.service.repository

import com.clemble.thank.model.User
import com.clemble.thank.model.error.{RepositoryError, RepositoryException}
import com.clemble.thank.test.util.UserGenerator
import org.junit.runner.RunWith
import org.specs2.concurrent.ExecutionEnv
import org.specs2.runner.JUnitRunner
import play.api.libs.iteratee.Iteratee

import scala.concurrent.Future
import scala.util.{Failure, Success}

@RunWith(classOf[JUnitRunner])
class UserRepositorySpec(implicit val ee: ExecutionEnv) extends RepositorySpec {

  val userRepository = application.injector.instanceOf[UserRepository]

  "CREATE" should {

    "support single create" in {
      val user = UserGenerator.generate()
      val fCreate: Future[User] = userRepository.create(user)
      fCreate must await(beEqualTo(user))

      val fSearch: Future[Option[User]] = userRepository.findById(user.id)
      fSearch must await(beEqualTo(Some(user)))
    }

    "throw Exception on multiple creation" in {
      val user = UserGenerator.generate()

      val fSecondCreate: Future[User] = for {
        _ <- userRepository.create(user)
        sCreate <- userRepository.create(user)
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

  "SEARCH" should {

    "find all" in {
      val A = UserGenerator.generate()
      val B = UserGenerator.generate()

      val fAll = for {
        _ <- userRepository.create(A)
        _ <- userRepository.create(B)
        all <- userRepository.findAll().run(Iteratee.fold(List.empty[User])((a, b) => b :: a))
      } yield {
        all
      }

      fAll must await(contain[User](A))
      fAll must await(contain[User](B))
    }

  }

}
