package com.clemble.loveit.payment.service.repository

import com.clemble.loveit.common.RepositorySpec
import com.clemble.loveit.common.model.{Resource, UserID}
import com.clemble.loveit.payment.model.PendingTransaction
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
      val res = someRandom[Resource]
      val A = PendingTransaction(someRandom[UserID], res)
      val B = PendingTransaction(someRandom[UserID], res)

      await(repo.save(user, A))
      await(repo.save(user, B))

      val userTransactions = repo.findByUser(user).toSeq()
      userTransactions.size shouldEqual 1
    }

    "save all payments for the user" in {
      val user = createUser()
      val A = PendingTransaction(someRandom[UserID], someRandom[Resource])
      val B = PendingTransaction(someRandom[UserID], someRandom[Resource])

      await(repo.save(user, A))
      await(repo.save(user, B))
      val transactions = repo.findByUser(user).toSeq

      transactions must containAllOf(Seq(A, B)).exactly
    }

    "remove specified" in {
      val user = createUser()
      val A = PendingTransaction(someRandom[UserID], someRandom[Resource])
      val B = PendingTransaction(someRandom[UserID], someRandom[Resource])

      await(repo.save(user, A))
      await(repo.save(user, B))

      await(repo.removeAll(user, Seq(A)))

      val afterRemove = repo.findByUser(user).toSeq
      afterRemove shouldEqual Seq(B)
    }

  }

}
