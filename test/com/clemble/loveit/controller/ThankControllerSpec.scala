package com.clemble.loveit.controller

import com.clemble.loveit.common.model.HttpResource
import com.clemble.loveit.thank.model.ResourceOwnership._
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
      val masterUrl = s"example.com/${randomNumeric(10)}/${randomNumeric(10)}"

      val giver = createUser()
      val owner = createUser()
      val ownerBalanceBefore = getMyUser()(owner).balance

      addOwnership(full(HttpResource(masterUrl)))(owner) shouldNotEqual None

      val uriVariations = ResourceSpec.generateVariations(masterUrl)
      val thanks = for {
        uri <- uriVariations
      } yield {
        val req = FakeRequest(PUT, s"/api/v1/thank/${uri}").withHeaders(giver:_*)
        route(application, req).get
      }
      val updateReq = await(Future.sequence(thanks)).map(_.header.status)
      updateReq.forall(_ == OK) should beEqualTo(true)

      getMyUser()(owner).balance shouldEqual ownerBalanceBefore + uriVariations.length
    }

    "create transaction" in {
      val masterUrl = s"example.com/${randomNumeric(10)}/${randomNumeric(10)}"

      val giver = createUser()
      val owner = createUser()
      addOwnership(full(HttpResource(masterUrl)))(owner) shouldNotEqual None

      val req = FakeRequest(PUT, s"/api/v1/thank/${masterUrl}").withHeaders(giver:_*)
      await(route(application, req).get)

      val giverTransactions = getMyPayments()(giver)
      val ownerTransactions = getMyPayments()(owner)

      giverTransactions.size shouldEqual 1
      ownerTransactions.size shouldEqual 1

      giverTransactions.head.amount shouldEqual 1L
      giverTransactions.head.operation shouldEqual Credit

      ownerTransactions.head.amount shouldEqual 1L
      ownerTransactions.head.operation shouldEqual Debit
    }

  }

}
