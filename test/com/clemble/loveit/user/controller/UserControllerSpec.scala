package com.clemble.loveit.user.controller

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
      val socialProfile = someRandom[CommonSocialProfile]
      val user = createUser(socialProfile)

      val savedUser = getMyUser(user)
      val expectedUser = (User from socialProfile).copy(id = savedUser.id, created = savedUser.created)
      savedUser must beEqualTo(expectedUser)
    }

    "Return same user on the same authentication" in {
      val socialProfile = someRandom[CommonSocialProfile]
      val firstUser = createUser(socialProfile)
      val firstAuth = ControllerSpec.getUser(firstUser)

      val secondUser = createUser(socialProfile)
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
      val setCookie = res.header.headers.get(SET_COOKIE)

      setCookie shouldNotEqual None

      val userCookie = setCookie.get
      val userId = setCookie.get.substring(7, userCookie.indexOf(";"))
      val expectedId = getMyUser(userId).id
      userId shouldEqual expectedId
    }

  }

}
