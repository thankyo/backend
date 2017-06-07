package com.clemble.loveit.payment.service.repository

import akka.stream.scaladsl.Sink
import com.clemble.loveit.common.RepositorySpec
import com.clemble.loveit.payment.model.ThankTransaction
import com.clemble.loveit.user.model.User
import org.junit.runner.RunWith
import org.specs2.concurrent.ExecutionEnv
import org.specs2.runner.JUnitRunner

import scala.collection.immutable.Seq

@RunWith(classOf[JUnitRunner])
class ThankTransactionRepositorySpec(implicit ee: ExecutionEnv) extends RepositorySpec {

  val repo = dependency[ThankTransactionRepository]

  "CREATE" should {

    "save all payments for the user" in {
      val user = createUser()
      val A = someRandom[ThankTransaction].copy(user = user.id)
      val B = someRandom[ThankTransaction].copy(user = user.id)

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
