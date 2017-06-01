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
      val ownerBalanceBefore = getMyUser()(owner).balance

      addOwnership(getMyUser()(owner).id, HttpResource(masterUrl))(owner) shouldNotEqual None

      val uriVariations = ResourceSpec.generateVariations(masterUrl)
      val thanks = for {
        uri <- uriVariations
      } yield {
        val req = FakeRequest(PUT, s"/api/v1/thank/http/${uri}").withHeaders(giver:_*)
        route(application, req).get.map(_.header.status).recoverWith({ case t => Future.successful(500) })
      }
      val updateReq = await(Future.sequence(thanks))
      updateReq.filter(_ == OK).size shouldEqual 1

      getMyUser()(owner).balance shouldEqual ownerBalanceBefore + 1
    }

    "create transaction" in {
      val masterUrl = s"${randomNumeric(10)}.com/${randomNumeric(10)}/${randomNumeric(10)}"

      val giver = createUser()
      val owner = createUser()
      addOwnership(getMyUser()(owner).id, HttpResource(masterUrl))(owner) shouldNotEqual None

      val req = FakeRequest(PUT, s"/api/v1/thank/http/${masterUrl}").withHeaders(giver:_*)
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
