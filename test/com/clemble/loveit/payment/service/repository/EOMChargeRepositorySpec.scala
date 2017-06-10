package com.clemble.loveit.payment.service.repository

import java.time.YearMonth

import com.clemble.loveit.common.RepositorySpec
import com.clemble.loveit.common.error.{RepositoryException, ThankException}
import com.clemble.loveit.payment.model.{EOMCharge}
import com.clemble.loveit.payment.model.ChargeStatus._
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner
import play.api.libs.json.Json

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
      val A = someRandom[EOMCharge].copy(yom = yom, status = Failed)
      val B = someRandom[EOMCharge].copy(yom = yom, status = Pending)

      await(repo.save(A))
      await(repo.save(B))

      val pending = repo.listPending(yom).toSeq()
      pending should not contain(A)
      pending should contain(B)
    }

    "list all pending" in {
      val yom = someRandom[YearMonth]
      val A = someRandom[EOMCharge].copy(yom = yom, status = Pending)
      val B = someRandom[EOMCharge].copy(yom = yom, status = Pending)

      await(repo.save(A))
      await(repo.save(B))

      val pending = repo.listPending(yom).toSeq()
      pending should containAllOf(Seq(A, B))
    }

  }

  "LIST successful" should {

    "list only Successful" in {
      val yom = someRandom[YearMonth]
      val A = someRandom[EOMCharge].copy(yom = yom, status = Failed)
      val B = someRandom[EOMCharge].copy(yom = yom, status = Success)

      await(repo.save(A))
      await(repo.save(B))

      val pending = repo.listSuccessful(yom).toSeq()
      pending should not contain(A)
      pending should contain(B)
    }

    "list all Successful" in {
      val yom = someRandom[YearMonth]
      val A = someRandom[EOMCharge].copy(yom = yom, status = Success)
      val B = someRandom[EOMCharge].copy(yom = yom, status = Success)

      await(repo.save(A))
      await(repo.save(B))

      val pending = repo.listSuccessful(yom).toSeq()
      pending should containAllOf(Seq(A, B))
    }

  }

  "UPDATE pending" should {

    "fail on nonexistent" in {
      val A = someRandom[EOMCharge].copy(status = Pending)

      await(repo.updatePending(A.user, A.yom, Failed, Json.obj())) shouldEqual false
    }

    "update pending" in {
      val A = someRandom[EOMCharge].copy(status = Pending)

      await(repo.save(A))

      await(repo.updatePending(A.user, A.yom, Failed, Json.obj())) shouldEqual true
      await(repo.updatePending(A.user, A.yom, Success, Json.obj())) shouldEqual false
    }

  }

}
