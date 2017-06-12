package com.clemble.loveit.payment.service.repository

import java.time.YearMonth

import com.clemble.loveit.common.RepositorySpec
import com.clemble.loveit.common.error.RepositoryException
import com.clemble.loveit.payment.model.PayoutStatus.{Failed, Pending, Success}
import com.clemble.loveit.payment.model.{EOMPayout}
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner
import play.api.libs.json.Json

@RunWith(classOf[JUnitRunner])
class EOMPayoutRepositorySpec extends RepositorySpec {

  val repo = dependency[EOMPayoutRepository]

  "CREATE" should {

    "simple create" in {
      val payout = someRandom[EOMPayout]

      await(repo.save(payout)) shouldEqual true

      val userPayouts = repo.findByUser(payout.user).toSeq()
      userPayouts shouldEqual Seq(payout)
    }

    "ignores same charges saved twice" in {
      val payout = someRandom[EOMPayout]

      await(repo.save(payout))
      await(repo.save(payout)) should throwA[RepositoryException]

      val userPayouts = repo.findByUser(payout.user).toSeq()
      userPayouts.size shouldEqual 1
      userPayouts must beEqualTo(Seq(payout))
    }

  }

  "LIST pending" should {

    "list only pending" in {
      val yom = someRandom[YearMonth]
      val A = someRandom[EOMPayout].copy(yom = yom, status = Failed)
      val B = someRandom[EOMPayout].copy(yom = yom, status = Pending)

      await(repo.save(A))
      await(repo.save(B))

      val pending = repo.listPending(yom).toSeq()
      pending should not contain (A)
      pending should contain(B)
    }

    "list all pending" in {
      val yom = someRandom[YearMonth]
      val A = someRandom[EOMPayout].copy(yom = yom, status = Pending)
      val B = someRandom[EOMPayout].copy(yom = yom, status = Pending)

      await(repo.save(A))
      await(repo.save(B))

      val pending = repo.listPending(yom).toSeq()
      pending should containAllOf(Seq(A, B))
    }

  }

  "UPDATE pending" should {

    "fail on nonexistent" in {
      val A = someRandom[EOMPayout].copy(status = Pending)

      await(repo.updatePending(A.user, A.yom, Failed, Json.obj())) shouldEqual false
    }

    "update pending" in {
      val A = someRandom[EOMPayout].copy(status = Pending)

      await(repo.save(A))

      await(repo.updatePending(A.user, A.yom, Failed, Json.obj())) shouldEqual true
      await(repo.updatePending(A.user, A.yom, Success, Json.obj())) shouldEqual false
    }

  }

}
