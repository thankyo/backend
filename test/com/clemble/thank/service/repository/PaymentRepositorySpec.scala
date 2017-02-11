package com.clemble.thank.service.repository

import com.clemble.thank.model.Payment
import com.clemble.thank.test.util.{PaymentGenerator, UserGenerator}
import org.specs2.concurrent.ExecutionEnv
import play.api.libs.iteratee.Iteratee

class PaymentRepositorySpec(implicit ee: ExecutionEnv) extends RepositorySpec {

  val repository = application.injector.instanceOf[PaymentRepository]

  "CREATE" should {

    "save all payments for the user" in {
      val user = UserGenerator.generate()
      val A = PaymentGenerator.generate(user)
      val B = PaymentGenerator.generate(user)

      val fTransactions = for {
        _ <- repository.save(A)
        _ <- repository.save(B)
        transactions <- repository.findByUser(user.id).run(Iteratee.fold(List.empty[Payment])((agg, b) => b :: agg))
      } yield {
        transactions
      }

      fTransactions must await(containAllOf(Seq(A, B)))
    }

  }

}
