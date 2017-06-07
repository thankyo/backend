package com.clemble.loveit.payment.service.repository

import com.clemble.loveit.common.RepositorySpec
import com.clemble.loveit.common.error.RepositoryException
import com.clemble.loveit.payment.model.BankDetails
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner

@RunWith(classOf[JUnitRunner])
class PaymentRepositorySpec extends RepositorySpec {

  val repo = dependency[PaymentRepository]

  "get balance" in {
    val A = createUser()
    await(repo.getBalance(A.id)) shouldEqual A.balance
  }

  "update balance" in {
    val A = createUser()
    await(repo.updateBalance(A.id, -100)  )
    await(repo.getBalance(A.id)) shouldEqual A.balance -100
  }

  "get bankDetails" in {
    val A = createUser()

    await(repo.getBankDetails(A.id)) shouldEqual A.bankDetails
  }

  "set same BankDetails" in {
    val A = createUser()
    val B = createUser()
    val bankDetails = someRandom[BankDetails]

    await(repo.setBankDetails(A.id, bankDetails)) shouldEqual true
    await(repo.setBankDetails(B.id, bankDetails)) should throwA[RepositoryException]

    await(repo.getBankDetails(A.id)) shouldEqual Some(bankDetails)
    await(repo.getBankDetails(B.id)) shouldNotEqual Some(bankDetails)
  }

}
