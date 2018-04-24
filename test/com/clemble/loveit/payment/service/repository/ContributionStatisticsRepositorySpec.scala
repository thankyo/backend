package com.clemble.loveit.payment.service.repository

import com.clemble.loveit.common.{ControllerSpec, RepositorySpec, ServiceSpec, ThankScenario}
import com.clemble.loveit.common.model.{Project, Resource, UserID}
import com.clemble.loveit.common.util.IDGenerator
import com.clemble.loveit.payment.controller.PaymentControllerTestExecutor
import com.clemble.loveit.payment.model.{ContributionStatistics, PendingTransaction}
import com.clemble.loveit.payment.service.{ContributionStatisticsService, PendingTransactionService}
import org.junit.runner.RunWith
import org.specs2.concurrent.ExecutionEnv
import org.specs2.runner.JUnitRunner
import play.api.libs.json.Json
import play.api.test.FakeRequest

import scala.util.Random

trait ContributionStatisticsScenario extends ThankScenario {

  def getContributions(user: UserID): ContributionStatistics

  def contribute(user: UserID, url: Resource = randomResource): Boolean

  "contributions counted" in {
    val user = createUser()

    val numContributions = Random.nextInt(20)
    (0 to numContributions) map (_ => contribute(user))

    getContributions(user) shouldEqual ContributionStatistics(user, numContributions + 1)
  }

  "contributions on new" in {
    val user = createUser()

    getContributions(user) shouldEqual ContributionStatistics(user, 0)
  }

}

// Need this separation, because there is no way to make unauthorized request without existing user
trait InternalContributionStatisticsScenario extends ContributionStatisticsScenario {

  "contributions on fake" in {
    val user = IDGenerator.generate()

    getContributions(user) shouldEqual ContributionStatistics(user, 0)
  }

}

@RunWith(classOf[JUnitRunner])
class ContributionStatisticsRepositorySpec(implicit ee: ExecutionEnv) extends InternalContributionStatisticsScenario with RepositorySpec {

  val repo: PendingTransactionRepository = dependency[PendingTransactionRepository]
  val statRepo: ContributionStatisticsRepository = dependency[ContributionStatisticsRepository]

  override def getContributions(user: UserID): ContributionStatistics = {
    await(statRepo.find(user))
  }

  override def contribute(user: UserID, url: Resource = randomResource): Boolean = {
    await(repo.save(user, PendingTransaction(someRandom[Project], url)))
  }

}

@RunWith(classOf[JUnitRunner])
class ContributionStatisticsServiceSpec(implicit ee: ExecutionEnv) extends InternalContributionStatisticsScenario with ServiceSpec {

  val trService: PendingTransactionService = dependency[PendingTransactionService]
  val statService: ContributionStatisticsService = dependency[ContributionStatisticsService]

  override def getContributions(user: UserID): ContributionStatistics = await(statService.find(user))

  override def contribute(user: UserID, url: Resource = randomResource): Boolean = {
    val project = createProject(createUser(), url)
    await(trService.create(user, project, url))
    true
  }

}

@RunWith(classOf[JUnitRunner])
class ContributionStatisticsControllerSpec(implicit ee: ExecutionEnv) extends ContributionStatisticsScenario with ControllerSpec with PaymentControllerTestExecutor{

  override def getContributions(user: UserID): ContributionStatistics = {
    val res = perform(user, FakeRequest(GET, "/api/v1/payment/statistic/my/contribution"))
    Json.parse(await(res.body.consumeData).utf8String).as[ContributionStatistics]
  }

  override def contribute(user: UserID, url: Resource = randomResource): Boolean = {
    val project = createProject()
    thank(user, project, url)
    true
  }

}
