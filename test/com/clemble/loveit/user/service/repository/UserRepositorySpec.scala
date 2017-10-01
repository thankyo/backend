package com.clemble.loveit.user.service.repository

import com.clemble.loveit.common.RepositorySpec
import com.clemble.loveit.user.model.User
import com.clemble.loveit.common.error.RepositoryException
import com.clemble.loveit.payment.model.ChargeAccount
import com.clemble.loveit.payment.service.repository.{UserBalanceRepository, PaymentAccountRepository}
import org.junit.runner.RunWith
import org.specs2.concurrent.ExecutionEnv
import org.specs2.runner.JUnitRunner

import scala.concurrent.Future

@RunWith(classOf[JUnitRunner])
class UserRepositorySpec(implicit val ee: ExecutionEnv) extends RepositorySpec {

  lazy val balanceRepo = dependency[PaymentAccountRepository with UserBalanceRepository]

  "CREATE" should {

    "create user" in {
      val user = someRandom[User]
      val createAndGet = userRepo.save(user).flatMap(_ => userRepo.findById(user.id))
      createAndGet must await(beEqualTo(Some(user)))
    }

    "return exception on creating the same" in {
      val user = someRandom[User]
      val createAndCreate = userRepo.save(user).flatMap(_ => userRepo.save(user)).flatMap(_ => userRepo.save(user))

      await(createAndCreate) should throwA[RepositoryException]
    }

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

    "set ChargeAccount" in {
      val A = createUser()
      val chAcc = someRandom[ChargeAccount]

      val matchResult = for {
        _ <- balanceRepo.setChargeAccount(A, chAcc)
        chargeAccount <- balanceRepo.getChargeAccount(A)
      } yield {
        chargeAccount must beEqualTo(Some(chAcc))
      }

      matchResult.await
    }

  }

  "CHANGE balance" should {

    "increase when positive" in {
      val user = createUser()

      val matchResult = for {
        balanceBefore <- balanceRepo.getBalance(user)
        _ <- balanceRepo.updateBalance(user, 10)
        balanceAfter <- balanceRepo.getBalance(user)
      } yield {
        balanceAfter shouldEqual balanceBefore + 10
      }

      matchResult.await
    }

    "decrease when negative" in {
      val user = createUser()

      val matchResult = for {
        balanceBefore <- balanceRepo.getBalance(user)
        _ <- balanceRepo.updateBalance(user, -10)
        balanceAfter <- balanceRepo.getBalance(user)
      } yield {
        balanceAfter shouldEqual balanceBefore - 10
      }

      matchResult.await
    }

  }

  "COUNT" should {
    val users = userRepo.find().toSeq().size
    val count = await(userRepo.count())
    users should beLessThanOrEqualTo(count)
  }

}
