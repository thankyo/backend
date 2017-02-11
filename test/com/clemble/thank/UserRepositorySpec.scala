package com.clemble.thank

import com.clemble.thank.model.User
import com.clemble.thank.model.error.{RepositoryError, RepositoryException}
import com.clemble.thank.service.repository.UserRepository
import com.clemble.thank.test.util.UserGenerator
import org.junit.runner.RunWith
import org.specs2.concurrent.ExecutionEnv
import org.specs2.mutable.Specification
import org.specs2.runner.JUnitRunner
import play.api.Mode
import play.api.inject.guice.GuiceApplicationBuilder

import scala.concurrent.Future
import scala.util.{Failure, Success}

@RunWith(classOf[JUnitRunner])
class UserRepositorySpec(implicit val ee: ExecutionEnv) extends Specification {

  val application = new GuiceApplicationBuilder().
    in(Mode.Test).
    build

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

//  def findAll(): Enumerator[User]
//
//  def findById(id: UserId): Future[Option[User]]
//
//  def create(user: User): Future[User]


}
