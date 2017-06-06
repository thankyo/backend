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
      val userAuth = createUser(socialProfile)

      val savedUser = getMyUser()(userAuth)
      val expectedUser = (User from socialProfile).copy(id = savedUser.id, created = savedUser.created)
      savedUser must beEqualTo(expectedUser)
    }

    "Return same user on the same authentication" in {
      val socialProfile = someRandom[CommonSocialProfile]
      val firstAuth = createUser(socialProfile)
      val firstUser = getMyUser()(firstAuth)

      val secondAuth = createUser(socialProfile)
      val secondUser = getMyUser()(firstAuth)

      firstAuth shouldNotEqual secondAuth
      secondUser shouldEqual firstUser
    }

    "sets a userId as a cookie" in {
      val json = Json.toJson(someRandom[CommonSocialProfile])
      val req = FakeRequest(POST, "/api/v1/auth/authenticate/test").
        withJsonBody(json)
      val fRes = route(application, req).get
      val res = await(fRes)
      val setCookie = res.header.headers.get(SET_COOKIE)

      setCookie shouldNotEqual None

      val userCookie = setCookie.get
      val userId = setCookie.get.substring(7, userCookie.indexOf(";"))
      val expectedId = getMyUser()(res.header.headers.toSeq).id
      userId shouldEqual expectedId
    }

  }

}
