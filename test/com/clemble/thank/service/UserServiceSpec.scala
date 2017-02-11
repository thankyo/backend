package com.clemble.thank.service

import com.clemble.thank.model.error.{RepositoryError, RepositoryException}
import com.clemble.thank.test.util.UserGenerator
import org.specs2.concurrent.ExecutionEnv

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

}
