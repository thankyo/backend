package com.clemble.loveit.payment.service.repository

import java.util.Currency

import com.clemble.loveit.common.RepositorySpec
import com.clemble.loveit.common.error.{PaymentException, RepositoryException}
import com.clemble.loveit.common.model.UserID
import com.clemble.loveit.payment.model.{ChargeAccount, Money, PayoutAccount}
import com.clemble.loveit.user.model.User
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner

@RunWith(classOf[JUnitRunner])
class PaymentRepositorySpec extends RepositorySpec {

  val repo = dependency[PaymentRepository]

  "get balance" in {
    val A = createUser()
    await(repo.getBalance(A.id)) shouldEqual 0
  }

  "update balance" in {
    val A = createUser()
    await(repo.updateBalance(A.id, -100)  )
    await(repo.getBalance(A.id)) shouldEqual -100
  }

  "PAYOUT ACCOUNT" should {

    "get" in {
      val A = createUser()
      await(repo.getPayoutAccount(A.id)) shouldEqual None
    }

    "set same" in {
      val A = createUser()
      val B = createUser()
      val ptAcc = someRandom[PayoutAccount]

      await(repo.setPayoutAccount(A.id, ptAcc)) shouldEqual true
      await(repo.setPayoutAccount(B.id, ptAcc)) should throwA[RepositoryException]

      await(repo.getPayoutAccount(A.id)) shouldEqual Some(ptAcc)
      await(repo.getPayoutAccount(B.id)) shouldNotEqual Some(ptAcc)
    }

  }


  "CHARGE ACCOUNT" should {

    "get chargeAccount" in {
      val A = createUser()

      await(repo.getChargeAccount(A.id)) shouldEqual None
    }

    "set same ChargeAccount" in {
      val A = createUser()
      val B = createUser()
      val chAcc = someRandom[ChargeAccount]

      await(repo.setChargeAccount(A.id, chAcc)) shouldEqual true
      await(repo.setChargeAccount(B.id, chAcc)) should throwA[RepositoryException]

      await(repo.getChargeAccount(A.id)) shouldEqual Some(chAcc)
      await(repo.getChargeAccount(B.id)) shouldNotEqual Some(chAcc)
    }

  }

  "LIMIT" should {

    "throw Exception" in {
      val user = createUser().id
      val negativeLimit = Money(-100, someRandom[Currency])

      await(repo.setMonthlyLimit(user, negativeLimit)) should throwA[PaymentException]
    }

    "update limit" in {
      val user = createUser().id
      val limit = Money(100, someRandom[Currency])

      await(repo.setMonthlyLimit(user, limit)) shouldEqual true
      await(repo.getMonthlyLimit(user)) shouldEqual Some(limit)
    }

    "return None on non existent user get" in {
      await(repo.getMonthlyLimit(someRandom[UserID])) shouldEqual None
    }

    "return false on non existent user set" in {
      val limit = Money(100, someRandom[Currency])
      await(repo.setMonthlyLimit(someRandom[UserID], limit)) shouldEqual false
    }

  }
}
