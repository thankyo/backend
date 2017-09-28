package com.clemble.loveit.user.controller

import com.clemble.loveit.auth.models.requests.SignUpRequest
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
      savedUser.firstName must beEqualTo(Some(profile.firstName))
      savedUser.lastName must beEqualTo(Some(profile.lastName))
      savedUser.email must beEqualTo(profile.email)
    }

    "Return same user on the same authentication" in {
      val profile = someRandom[SignUpRequest]
      val firstUser = createUser(profile)
      val firstAuth = ControllerSpec.getUser(firstUser)

      val secondUser = createUser(profile)
      val secondAuth = ControllerSpec.getUser(secondUser)

      firstAuth shouldNotEqual secondAuth
      secondUser shouldEqual firstUser
    }

    "sets a userId as a cookie" in {
      val json = Json.toJson(someRandom[CommonSocialProfile])
      val req = FakeRequest(POST, "/api/v1/auth/authenticate/test").
        withJsonBody(json)
      val res = await(route(application, req).get)

      ControllerSpec.setUser(res)
      val setCookie = res.newCookies

      setCookie.size shouldEqual 1

      val userId = setCookie(0).value
      val expectedId = getMyUser(userId).id
      userId shouldEqual expectedId
    }

  }

}
