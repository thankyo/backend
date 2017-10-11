package com.clemble.loveit.user.controller

import com.clemble.loveit.auth.model.requests.SignUpRequest
import com.clemble.loveit.common.ControllerSpec
import com.clemble.loveit.user.model.User
import com.mohiva.play.silhouette.impl.providers.CommonSocialProfile
import org.junit.runner.RunWith
import org.specs2.concurrent.ExecutionEnv
import org.specs2.runner.JUnitRunner
import play.api.libs.json.Json
import com.clemble.loveit.user.model.User.socialProfileJsonFormat
import play.api.test.FakeRequest

@RunWith(classOf[JUnitRunner])
class UserControllerSpec(implicit ee: ExecutionEnv) extends ControllerSpec {

  "CREATE" should {

    "Support single create" in {
      val profile = someRandom[SignUpRequest]
      val user = createUser(profile)

      val savedUser = getMyUser(user)
      savedUser.firstName must beSome(profile.firstName)
      savedUser.lastName must beSome(profile.lastName)
      savedUser.email must beEqualTo(profile.email)
    }

    "Forbids double authentication" in {
      val profile = someRandom[SignUpRequest]

      createUser(profile)
      createUser(profile) must throwA[Exception]()
    }

    "sets a userId as a cookie" in {
      val signUpReq = Json.toJson(someRandom[SignUpRequest])
      val req = FakeRequest(POST, "/api/v1/auth/signUp").withJsonBody(signUpReq)
      val res = await(route(application, req).get)

      ControllerSpec.setUser(res)
      val setCookie = res.newCookies

      setCookie.size shouldEqual 1

      val userId = setCookie.head.value
      val expectedId = getMyUser(userId).id
      userId shouldEqual expectedId
    }

  }

}
