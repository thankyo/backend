package com.clemble.loveit.thank.controller

import com.clemble.loveit.payment.controller.PaymentControllerTestExecutor
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner
import play.api.libs.json.Json
import play.api.test.FakeRequest

import scala.concurrent.Future

@RunWith(classOf[JUnitRunner])
class GraphControllerSpec extends PaymentControllerTestExecutor {

  "UPDATE" should {

    "support different format types for FULL" in {
      val masterUrl = s"http://${someRandom[Long]}.com/${someRandom[Long]}/${someRandom[Long]}"

      val giver = createUser()
      val owner = createUser()

      createProject(owner, masterUrl) shouldNotEqual None

      val urlVariations = List(masterUrl)
      val thanks = for {
        url <- urlVariations
      } yield {
        val res = perform(giver, FakeRequest(POST, s"/api/v1/thank/graph/my/support").withJsonBody(Json.obj("url" -> url)))
        res.header.status
      }
      val allSuccess = thanks.forall(_ == OK)
      allSuccess should beTrue
    }

    "create transaction" in {
      val masterUrl = s"http://${someRandom[Long]}.com/${someRandom[Long]}/${someRandom[Long]}"

      val giver = createUser()
      val owner = createUser()
      createProject(getMyUser(owner).id, masterUrl) shouldNotEqual None

      perform(giver, FakeRequest(POST, s"/api/v1/thank/graph/my/support").withJsonBody(Json.obj("url" -> masterUrl)))

      val giverTransactions = pendingCharges(giver)
      val ownerTransactions = pendingCharges(owner)

      giverTransactions.size shouldEqual 1
      ownerTransactions.size shouldEqual 0
    }

  }

}
