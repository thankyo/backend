package com.clemble.loveit.payment.service.repository

import com.clemble.loveit.common.RepositorySpec
import com.clemble.loveit.common.error.RepositoryException
import com.clemble.loveit.payment.model.PayoutAccount
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner

@RunWith(classOf[JUnitRunner])
class PayoutAccountRepositorySpec extends RepositorySpec {

  val repo = dependency[PayoutAccountRepository]

  "PAYOUT ACCOUNT" should {

    "get" in {
      val A = createUser()
      await(repo.getPayoutAccount(A)) shouldEqual None

      val ptAcc = someRandom[PayoutAccount]
      await(repo.setPayoutAccount(A, ptAcc)) shouldEqual true

      await(repo.getPayoutAccount(A)) shouldEqual Some(ptAcc)
    }

    "set same" in {
      val A = createUser()
      val B = createUser()
      val ptAcc = someRandom[PayoutAccount]

      await(repo.setPayoutAccount(A, ptAcc)) shouldEqual true
      await(repo.setPayoutAccount(B, ptAcc)) should throwA[RepositoryException]

      await(repo.getPayoutAccount(A)) shouldEqual Some(ptAcc)
      await(repo.getPayoutAccount(B)) shouldNotEqual Some(ptAcc)
    }

    "delete" in {
      val A = createUser()
      await(repo.getPayoutAccount(A)) shouldEqual None

      val ptAcc = someRandom[PayoutAccount]
      await(repo.setPayoutAccount(A, ptAcc)) shouldEqual true

      await(repo.getPayoutAccount(A)) shouldEqual Some(ptAcc)

      await(repo.deletePayoutAccount(A)) shouldEqual true
      await(repo.getPayoutAccount(A)) shouldEqual None
    }

  }
}
