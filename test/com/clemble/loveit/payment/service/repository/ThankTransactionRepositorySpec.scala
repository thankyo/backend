package com.clemble.loveit.payment.service.repository

import akka.stream.scaladsl.Sink
import com.clemble.loveit.common.RepositorySpec
import com.clemble.loveit.common.model.{Resource, UserID}
import com.clemble.loveit.payment.model.ThankTransaction
import org.junit.runner.RunWith
import org.specs2.concurrent.ExecutionEnv
import org.specs2.runner.JUnitRunner

import scala.collection.immutable.Seq

@RunWith(classOf[JUnitRunner])
class ThankTransactionRepositorySpec(implicit ee: ExecutionEnv) extends RepositorySpec {

  val repo = dependency[ThankTransactionRepository]

  "CREATE" should {

    "same resource transactions saved only once" in {
      val user = createUser().id
      val res = someRandom[Resource]
      val A = ThankTransaction(user, someRandom[UserID], res)
      val B = ThankTransaction(user, someRandom[UserID], res)

      await(repo.save(A))
      await(repo.save(B))

      val userTransactions = await(repo.findByUser(user).runWith(Sink.seq[ThankTransaction]))
      userTransactions.size shouldEqual 1
    }

    "save all payments for the user" in {
      val user = createUser().id
      val A = ThankTransaction(user, someRandom[UserID], someRandom[Resource])
      val B = ThankTransaction(user, someRandom[UserID], someRandom[Resource])

      val fTransactions = for {
        _ <- repo.save(A)
        _ <- repo.save(B)
        transactions <- repo.findByUser(user).runWith(Sink.seq[ThankTransaction])
      } yield {
        transactions
      }
      val transactions = await(fTransactions)

      transactions must containAllOf(Seq(A, B)).exactly
    }

  }

}
