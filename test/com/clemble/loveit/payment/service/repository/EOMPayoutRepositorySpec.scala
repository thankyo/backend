package com.clemble.loveit.payment.service.repository

import com.clemble.loveit.common.RepositorySpec
import com.clemble.loveit.common.error.RepositoryException
import com.clemble.loveit.payment.model.{EOMPayout}
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner

@RunWith(classOf[JUnitRunner])
class EOMPayoutRepositorySpec extends RepositorySpec{

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

}
