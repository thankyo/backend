package com.clemble.loveit.thank.controller

import com.clemble.loveit.common.ControllerSpec
import com.clemble.loveit.common.model.Resource
import com.mohiva.play.silhouette.impl.providers.CommonSocialProfile
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner
import play.api.libs.json.Json
import play.api.test.FakeRequest

@RunWith(classOf[JUnitRunner])
class ResourceOwnershipControllerSpec extends ControllerSpec {

  def listResources(userAuth: Seq[(String, String)]): Seq[Resource] = {
    val req = FakeRequest(GET, s"/api/v1/thank/ownership/my").withHeaders(userAuth:_*)
    val fRes = route(application, req).get

    val res = await(fRes)
    val respSource = res.body.consumeData.map(_.utf8String).map(Json.parse(_)).map(_.as[Seq[Resource]])
    await(respSource)
  }

  "GET" should {

    "List on new user" in {
      val social = someRandom[CommonSocialProfile]
      val userAuth = createUser(social)

      val resources = listResources(userAuth)
      resources shouldEqual List.empty
    }

  }

}
