package com.clemble.loveit.service.repository

import akka.stream.scaladsl.Sink
import com.clemble.loveit.model.{ThankTransaction}
import com.clemble.loveit.test.util.{ThankTransactionGenerator, UserGenerator}
import org.junit.runner.RunWith
import org.specs2.concurrent.ExecutionEnv
import org.specs2.runner.JUnitRunner

import scala.collection.immutable.Seq

@RunWith(classOf[JUnitRunner])
class ThankTransactionRepositorySpec(implicit ee: ExecutionEnv) extends RepositorySpec {

  val repo = application.injector.instanceOf[ThankTransactionRepository]

  "CREATE" should {

    "save all payments for the user" in {
      val user = UserGenerator.generate()
      val A = ThankTransactionGenerator.generate(user)
      val B = ThankTransactionGenerator.generate(user)

      val fTransactions = for {
        _ <- repo.save(A)
        _ <- repo.save(B)
        transactions <- repo.findByUser(user.id).runWith(Sink.seq[ThankTransaction])
      } yield {
        transactions
      }
      val transactions = await(fTransactions)

      transactions must containAllOf(Seq(A, B)).exactly
    }

  }

}
