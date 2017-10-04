package com.clemble.loveit.payment.service.repository

import java.time.{LocalDateTime, YearMonth}

import com.clemble.loveit.common.RepositorySpec
import com.clemble.loveit.common.error.RepositoryException
import com.clemble.loveit.payment.model.{EOMStatistics, EOMStatus}
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner

@RunWith(classOf[JUnitRunner])
class EOMStatusRepositorySpec extends RepositorySpec {

  val repo = dependency[EOMStatusRepository]

  "SAVE" should {

    "create new" in {
      val status = someRandom[EOMStatus]

      await(repo.save(status)) shouldEqual status
    }

    "throw error on second creation" in {
      val status = someRandom[EOMStatus]
      val otherStatus = someRandom[EOMStatus].copy(yom = status.yom)

      await(repo.save(status)) shouldEqual status
      await(repo.save(otherStatus)) should throwA[RepositoryException]
    }

  }

  "GET" should {
    "get saved" in {
      val status = someRandom[EOMStatus]

      await(repo.save(status))
      await(repo.get(status.yom)) shouldEqual Some(status)
    }

  }

  "UPDATE" should {

    "update" in {
      val yom = someRandom[YearMonth]
      val status = someRandom[EOMStatus].copy(yom = yom)

      await(repo.save(status))

      val updatedStatus = someRandom[EOMStatus].copy(yom = status.yom, finished = Some(someRandom[LocalDateTime]))

      val applyCharges = someRandom[EOMStatistics]
      await(repo.updateApplyCharges(yom, applyCharges))
      await(repo.get(yom)).flatMap(_.applyCharges) shouldEqual Some(applyCharges)

      val createCharges = someRandom[EOMStatistics]
      await(repo.updateCreateCharges(yom, createCharges))
      await(repo.get(yom)).flatMap(_.createCharges) shouldEqual Some(createCharges)

      val createPayout = someRandom[EOMStatistics]
      await(repo.updateCreatePayout(yom, createPayout))
      await(repo.get(yom)).flatMap(_.createPayout) shouldEqual Some(createPayout)

      val applyPayout = someRandom[EOMStatistics]
      await(repo.updateApplyPayout(yom, applyPayout))
      await(repo.get(yom)).flatMap(_.applyPayout) shouldEqual Some(applyPayout)

      val finished = someRandom[LocalDateTime]
      await(repo.updateFinished(yom, finished))
      await(repo.get(yom)).flatMap(_.finished) shouldEqual Some(finished)
    }

  }

}
