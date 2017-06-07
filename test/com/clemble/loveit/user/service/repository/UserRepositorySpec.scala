package com.clemble.loveit.user.service.repository

import com.clemble.loveit.common.RepositorySpec
import com.clemble.loveit.user.model.User
import com.clemble.loveit.common.error.{RepositoryException}
import com.clemble.loveit.payment.model.BankDetails
import com.clemble.loveit.payment.service.repository.PaymentRepository
import org.apache.commons.lang3.RandomStringUtils
import org.junit.runner.RunWith
import org.specs2.concurrent.ExecutionEnv
import org.specs2.runner.JUnitRunner

import scala.concurrent.Future
import scala.util.{Failure, Success}

@RunWith(classOf[JUnitRunner])
class UserRepositorySpec(implicit val ee: ExecutionEnv) extends RepositorySpec {

  lazy val balanceService = dependency[PaymentRepository]

  "CREATE" should {

    "support single create" in {
      val user = someRandom[User]
      val fCreate: Future[User] = userRepo.save(user)
      fCreate must await(beEqualTo(user))

      eventually(await(userRepo.findById(user.id)) must beEqualTo(Some(user)))
    }

    "throw Exception on multiple creation" in {
      val user = someRandom[User]

      val fSecondCreate: Future[User] = for {
        _ <- userRepo.save(user)
        sCreate <- userRepo.save(user)
      } yield {
        sCreate
      }

      val fSecondRes = fSecondCreate
      await(fSecondRes) should throwA[RepositoryException]
    }

    "throw Exception on same account used more then once creation" in {
      val A = createUser()
      val B = createUser()

      await(balanceService.setBankDetails(A.id, B.bankDetails.get)) must throwA[RepositoryException]
    }

    "set BankDetails" in {
      val A = someRandom[User]
      val bankDetails = BankDetails.payPal(RandomStringUtils.random(10))

      await(userRepo.save(A))
      await(balanceService.setBankDetails(A.id, bankDetails))

      await(userRepo.findById(A.id)).get.bankDetails must beEqualTo(Some(bankDetails))
      await(balanceService.getBankDetails(A.id)) must beEqualTo(Some(bankDetails))
    }

  }

  "CHANGE balance" should {

    "increase when positive" in {
      val user = someRandom[User]

      val matchResult = for {
        savedUser <- userRepo.save(user)
        _ <- balanceService.updateBalance(user.id, 10)
        updatedUser <- userRepo.findById(user.id).map(_.get)
      } yield {
        savedUser.balance shouldEqual user.balance
        updatedUser.balance shouldEqual user.balance + 10
      }

      matchResult.await
    }

    "decrease when negative" in {
      val user = someRandom[User]

      val matchResult = for {
        savedUser <- userRepo.save(user)
        _ <- balanceService.updateBalance(user.id, -10)
        updatedUser <- userRepo.findById(user.id).map(_.get)
      } yield {
        savedUser.balance shouldEqual user.balance
        updatedUser.balance shouldEqual user.balance -10
      }

      matchResult.await
    }

  }

}
