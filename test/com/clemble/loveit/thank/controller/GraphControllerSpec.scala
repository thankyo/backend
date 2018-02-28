package com.clemble.loveit.thank.controller

import com.clemble.loveit.common.model.HttpResource
import com.clemble.loveit.payment.controller.PaymentControllerTestExecutor
import com.clemble.loveit.user.model.ResourceSpec
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner
import play.api.libs.json.Json
import play.api.test.FakeRequest

import scala.concurrent.Future

@RunWith(classOf[JUnitRunner])
class GraphControllerSpec extends PaymentControllerTestExecutor {

  "UPDATE" should {

    "support different format types for FULL" in {
      val masterUrl = s"${someRandom[Long]}.com/${someRandom[Long]}/${someRandom[Long]}"

      val giver = createUser()
      val owner = createUser()

      createProject(owner, HttpResource(masterUrl)) shouldNotEqual None

      val urlVariations = ResourceSpec.generateVariations(masterUrl)
      val thanks = for {
        url <- urlVariations
      } yield {
        val req = sign(giver, FakeRequest(POST, s"/api/v1/thank/graph/my/support").withJsonBody(Json.obj("url" -> url)))
        route(application, req).get.map(_.header.status).recover({ case _ => 500 })
      }
      val updateReq = await(Future.sequence(thanks))
      val allSuccess = updateReq.forall(_ == OK)
      allSuccess should beTrue
    }

    "create transaction" in {
      val masterUrl = s"${someRandom[Long]}.com/${someRandom[Long]}/${someRandom[Long]}"

      val giver = createUser()
      val owner = createUser()
      createProject(getMyUser(owner).id, HttpResource(masterUrl)) shouldNotEqual None

      val req = sign(giver, FakeRequest(POST, s"/api/v1/thank/graph/my/support").withJsonBody(Json.obj("url" -> masterUrl)))
      await(route(application, req).get)

      val giverTransactions = pendingCharges(giver)
      val ownerTransactions = pendingCharges(owner)

      giverTransactions.size shouldEqual 1
      ownerTransactions.size shouldEqual 0
    }

  }

}
