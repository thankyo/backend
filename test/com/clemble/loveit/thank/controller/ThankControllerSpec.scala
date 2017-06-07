package com.clemble.loveit.thank.controller

import com.clemble.loveit.common.ControllerSpec
import com.clemble.loveit.common.model.HttpResource
import com.clemble.loveit.payment.model.{Credit, Debit}
import com.clemble.loveit.user.model.ResourceSpec
import org.apache.commons.lang3.RandomStringUtils._
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner
import play.api.test.FakeRequest

import scala.concurrent.Future

@RunWith(classOf[JUnitRunner])
class ThankControllerSpec extends ControllerSpec {

  "UPDATE" should {

    "support different format types for FULL" in {
      val masterUrl = s"${randomNumeric(20)}.com/${randomNumeric(10)}/${randomNumeric(10)}"

      val giver = createUser()
      val owner = createUser()
      val ownerBalanceBefore = getMyUser(owner).balance

      addOwnership(owner, HttpResource(masterUrl)) shouldNotEqual None

      val uriVariations = ResourceSpec.generateVariations(masterUrl)
      val thanks = for {
        uri <- uriVariations
      } yield {
        val req = sign(giver, FakeRequest(PUT, s"/api/v1/thank/http/${uri}"))
        route(application, req).get.map(_.header.status).recover({ case _ => 500 })
      }
      val updateReq = await(Future.sequence(thanks))
      updateReq.forall(_ == OK) should beTrue

      getMyUser(owner).balance shouldEqual ownerBalanceBefore + 1
    }

    "create transaction" in {
      val masterUrl = s"${randomNumeric(10)}.com/${randomNumeric(10)}/${randomNumeric(10)}"

      val giver = createUser()
      val owner = createUser()
      addOwnership(getMyUser(owner).id, HttpResource(masterUrl)) shouldNotEqual None

      val req = sign(giver, FakeRequest(PUT, s"/api/v1/thank/http/${masterUrl}"))
      await(route(application, req).get)

      val giverTransactions = getMyPayments(giver)
      val ownerTransactions = getMyPayments(owner)

      giverTransactions.size shouldEqual 1
      ownerTransactions.size shouldEqual 1
    }

  }

}
