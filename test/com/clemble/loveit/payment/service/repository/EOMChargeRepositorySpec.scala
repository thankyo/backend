package com.clemble.loveit.payment.service.repository

import java.time.YearMonth

import com.clemble.loveit.common.RepositorySpec
import com.clemble.loveit.common.error.RepositoryException
import com.clemble.loveit.payment.model.{ChargeStatus, EOMCharge}
import org.joda.time.DateTime
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner

@RunWith(classOf[JUnitRunner])
class EOMChargeRepositorySpec extends RepositorySpec {

  val repo = dependency[EOMChargeRepository]

  "CREATE" should {

    "simple create" in {
      val charge = someRandom[EOMCharge]

      val savedCharge = await(repo.save(charge))

      charge must beEqualTo(savedCharge)
    }

    "ignores same charges saved twice" in {
      val charge = someRandom[EOMCharge]

      await(repo.save(charge))
      await(repo.save(charge)) should throwA[RepositoryException]

      val userCharges = repo.findByUser(charge.user).toSeq()
      userCharges.size shouldEqual 1
      userCharges must beEqualTo(Seq(charge))
    }

  }

  "LIST pending" should {

    "list only pending" in {
      val yom = someRandom[YearMonth]
      val A = someRandom[EOMCharge].copy(yom = yom, status = ChargeStatus.Failed)
      val B = someRandom[EOMCharge].copy(yom = yom, status = ChargeStatus.Pending)

      await(repo.save(A))
      await(repo.save(B))

      val pending = repo.listPending(yom).toSeq()
      pending should not contain(A)
      pending should contain(B)
    }

    "list all pending" in {
      val yom = someRandom[YearMonth]
      val A = someRandom[EOMCharge].copy(yom = yom, status = ChargeStatus.Pending)
      val B = someRandom[EOMCharge].copy(yom = yom, status = ChargeStatus.Pending)

      await(repo.save(A))
      await(repo.save(B))

      val pending = repo.listPending(yom).toSeq()
      pending should containAllOf(Seq(A, B))
    }

  }
}
