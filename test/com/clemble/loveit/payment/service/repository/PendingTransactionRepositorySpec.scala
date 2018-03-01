package com.clemble.loveit.payment.service.repository

import com.clemble.loveit.common.RepositorySpec
import com.clemble.loveit.common.model.{Resource}
import com.clemble.loveit.payment.model.PendingTransaction
import com.clemble.loveit.thank.model.Project
import org.junit.runner.RunWith
import org.specs2.concurrent.ExecutionEnv
import org.specs2.runner.JUnitRunner

import scala.collection.immutable.Seq

@RunWith(classOf[JUnitRunner])
class PendingTransactionRepositorySpec(implicit ee: ExecutionEnv) extends RepositorySpec {

  val repo = dependency[PendingTransactionRepository]

  "CREATE" should {

    "same resource transactions saved only once" in {
      val user = createUser()
      val res = randomResource
      val A = PendingTransaction(someRandom[Project], res)
      val B = PendingTransaction(someRandom[Project], res)

      await(repo.save(user, A))
      await(repo.save(user, B))

      val userTransactions = await(repo.findChargesByUser(user))
      userTransactions.size shouldEqual 1
    }

    "save all payments for the user" in {
      val user = createUser()
      val A = PendingTransaction(someRandom[Project], randomResource)
      val B = PendingTransaction(someRandom[Project], randomResource)

      await(repo.save(user, A))
      await(repo.save(user, B))
      val transactions = await(repo.findChargesByUser(user))

      transactions must containAllOf(Seq(A, B)).exactly
    }

    "remove specified" in {
      val user = createUser()
      val A = PendingTransaction(someRandom[Project], randomResource)
      val B = PendingTransaction(someRandom[Project], randomResource)

      await(repo.save(user, A))
      await(repo.save(user, B))

      await(repo.removeCharges(user, Seq(A)))

      val afterRemove = await(repo.findChargesByUser(user))
      afterRemove shouldEqual Seq(B)
    }

  }

}
