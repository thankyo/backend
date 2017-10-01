package com.clemble.loveit.thank.controller

import com.clemble.loveit.common.ControllerSpec
import com.clemble.loveit.common.model.Resource
import com.clemble.loveit.thank.model.UserResource
import com.mohiva.play.silhouette.impl.providers.CommonSocialProfile
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner
import play.api.libs.json.Json
import play.api.test.FakeRequest

@RunWith(classOf[JUnitRunner])
class ROControllerSpec extends ControllerSpec {

  def listResources(user: String): UserResource = {
    val req = sign(user, FakeRequest(GET, s"/api/v1/thank/my/resource"))
    val res = await(route(application, req).get)

    val respSource = res.body.consumeData.map(_.utf8String).map(Json.parse(_).as[UserResource])
    await(respSource)
  }

  "GET" should {

    "List on new user" in {
      val user = createUser()

      val resources = listResources(user)
      resources shouldEqual UserResource(user)
    }

  }

}
