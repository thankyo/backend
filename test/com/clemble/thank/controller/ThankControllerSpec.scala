package com.clemble.thank.controller

import com.clemble.thank.model.ResourceOwnership
import com.clemble.thank.test.util.UserGenerator
import com.clemble.thank.util.URIUtilsSpec
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
      addOwnership(owner, ResourceOwnership.full(masterUrl))

      val uriVariations = URIUtilsSpec.generateVariations(masterUrl)
      val thanks = for {
        uri <- uriVariations
      } yield {
        val req = FakeRequest(PUT, s"/api/v1/thank/${uri}?user=${giver.id}")
        route(application, req).get
      }
      val updateReq = await(Future.sequence(thanks)).map(_.header.status)
      updateReq.forall(_ == OK) should beEqualTo(true)

      getUser(owner.id).get.balance shouldEqual uriVariations.length
    }

  }

}
