package com.clemble.loveit.payment.service.repository

import java.time.YearMonth

import com.clemble.loveit.common.RepositorySpec
import com.clemble.loveit.common.error.RepositoryException
import com.clemble.loveit.payment.model.{EOMStatistics, EOMStatus}
import org.joda.time.DateTime
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

      val updatedStatus = someRandom[EOMStatus].copy(yom = status.yom, finished = Some(someRandom[DateTime]))
      await(repo.update(yom,
        createCharges = updatedStatus.createCharges,
        applyCharges = updatedStatus.applyCharges,
        createPayout = updatedStatus.createPayout,
        applyPayout = updatedStatus.applyPayout,
        finished = updatedStatus.finished.get
      ))

      await(repo.get(yom)) shouldEqual Some(updatedStatus.copy(created = status.created))
    }

  }

}
