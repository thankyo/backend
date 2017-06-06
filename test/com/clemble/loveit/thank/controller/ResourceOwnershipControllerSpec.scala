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
class ResourceOwnershipControllerSpec extends ControllerSpec {

  def listResources(userAuth: String): UserResource = {
    val req = sign(userAuth, FakeRequest(GET, s"/api/v1/thank/resource/my"))
    val fRes = route(application, req).get

    val res = await(fRes)
    val respSource = res.body.consumeData.map(_.utf8String).map(Json.parse(_).as[UserResource])
    await(respSource)
  }

  "GET" should {

    "List on new user" in {
      val social = someRandom[CommonSocialProfile]
      val user = createUser(social)

      val resources = listResources(user)
      val expectedUserRes = Json.toJson(getMyUser(user)).as[UserResource]
      resources shouldEqual expectedUserRes
    }

  }

}
