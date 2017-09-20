package com.clemble.loveit.common.controller

import akka.util.ByteString
import com.clemble.loveit.common.ThankSpecification
import com.clemble.loveit.common.model.UserID
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner
import play.api.http.HttpEntity.Strict
import play.api.mvc.{Cookie, ResponseHeader, Result}
import play.api.test.FakeRequest

@RunWith(classOf[JUnitRunner])
class CookieUtilsSpec extends ThankSpecification {

  "READ cookie" in {
    val user = someRandom[UserID]
    val res = Result(ResponseHeader(200), Strict(ByteString.empty, None))
    val resWithCookie = CookieUtils.setUser(res, user)

    val userCookie = resWithCookie.newCookies.head

    val req = FakeRequest().withCookies(Cookie(userCookie.name, userCookie.value))
    val readUser = CookieUtils.readUser(req)

    readUser shouldEqual Some(user)
  }

}
