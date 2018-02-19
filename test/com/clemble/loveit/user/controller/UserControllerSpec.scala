package com.clemble.loveit.user.controller

import com.clemble.loveit.auth.model.requests.RegistrationRequest
import com.clemble.loveit.common.ControllerSpec
import com.clemble.loveit.common.controller.CookieUtils
import org.junit.runner.RunWith
import org.specs2.concurrent.ExecutionEnv
import org.specs2.runner.JUnitRunner
import play.api.libs.json.Json
import com.mohiva.play.silhouette.api.crypto.Base64
import play.api.test.FakeRequest

@RunWith(classOf[JUnitRunner])
class UserControllerSpec(implicit ee: ExecutionEnv) extends ControllerSpec {

  val cookieUtil = dependency[CookieUtils]

  "CREATE" should {

    "Support single create" in {
      val profile = someRandom[RegistrationRequest]
      val user = createUser(profile)

      val savedUser = getMyUser(user)
      savedUser.firstName must beSome(profile.firstName)
      savedUser.lastName must beSome(profile.lastName)
      savedUser.email must beEqualTo(profile.email)
    }

    "Forbids double authentication" in {
      val profile = someRandom[RegistrationRequest]

      createUser(profile)
      createUser(profile.copy(password = someRandom[String])) must throwA[Exception]()
    }

    "sets a userId as a cookie" in {
      val registerReq = Json.toJson(someRandom[RegistrationRequest])
      val req = FakeRequest(POST, "/api/v1/auth/register").withJsonBody(registerReq)
      val res = await(route(application, req).get)

      ControllerSpec.setUser(res)
      val setCookie = res.newCookies

      setCookie.size shouldEqual 1

      val userId = cookieUtil.crypter.decrypt(Base64.decode(setCookie.head.value))
      val expectedId = getMyUser(userId).id
      userId shouldEqual expectedId
    }

  }

}
