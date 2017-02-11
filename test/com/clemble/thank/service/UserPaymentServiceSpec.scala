package com.clemble.thank.service

import com.clemble.thank.model.Payment
import com.clemble.thank.test.util.UserGenerator
import org.specs2.concurrent.ExecutionEnv
import play.api.libs.iteratee.Iteratee

class UserPaymentServiceSpec(implicit ee: ExecutionEnv) extends ServiceSpec {

  val paymentService = application.injector.instanceOf[UserPaymentService]
  val userService = application.injector.instanceOf[UserService]

  "PAYMENT" should {

    "Debit increases User balance" in {
      val user = UserGenerator.generate()
      val matchResult = for {
        savedUser <- userService.create(user)
        _ <- paymentService.debit(user, 100)
        readUser <- userService.get(user.id).map(_.get)
      } yield {
        savedUser.balance shouldEqual 0
        readUser.balance shouldEqual 100
      }
      matchResult.await
    }

    "Credit decrease User balance" in {
      val user = UserGenerator.generate()
      val matchResult = for {
        savedUser <- userService.create(user)
        _ <- paymentService.debit(user, 100)
        _ <- paymentService.credit(user, 10)
        readUser <- userService.get(user.id).map(_.get)
      } yield {
        savedUser.balance shouldEqual 0
        readUser.balance shouldEqual 90
      }
      matchResult.await
    }

    "list all transactions" in {
      val user = UserGenerator.generate()
      val matchResult = for {
        _ <- userService.create(user)
        A <- paymentService.debit(user, 100)
        B <- paymentService.credit(user, 10)
        payments <- paymentService.payments(user).run(Iteratee.fold(List.empty[Payment]){ (agg, el) => el :: agg})
      } yield {
        payments must containAllOf(Seq(A, B))
      }
      matchResult.await
    }

  }

}
