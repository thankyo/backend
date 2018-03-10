package com.clemble.loveit.payment.service.repository

import com.clemble.loveit.common.RepositorySpec
import com.clemble.loveit.common.util.IDGenerator
import com.clemble.loveit.payment.model.{ContributionStatistics, PendingTransaction}
import org.junit.runner.RunWith
import org.specs2.concurrent.ExecutionEnv
import org.specs2.runner.JUnitRunner

import scala.concurrent.Future
import scala.util.Random

@RunWith(classOf[JUnitRunner])
class ContributionStatisticsRepositorySpec(implicit ee: ExecutionEnv) extends RepositorySpec {

  val repo = dependency[PendingTransactionRepository]
  val statRepo = dependency[ContributionStatisticsRepository]

  "contributions counted" in {
    val user = createUser()

    val numContributions = Random.nextInt(20)

    val transactions = (0 to numContributions) map (_ => repo.save(user, someRandom[PendingTransaction]))
    await(Future.sequence(transactions))

    await(statRepo.find(user)) shouldEqual ContributionStatistics(user, numContributions + 1)
  }

  "contributions on new" in {
    val user = createUser()

    await(statRepo.find(user)) shouldEqual ContributionStatistics(user, 0)
  }

  "contributions on fake" in {
    val user = IDGenerator.generate()

    await(statRepo.find(user)) shouldEqual ContributionStatistics(user, 0)
  }

}
