package com.clemble.loveit.common.controller

import akka.util.ByteString
import com.clemble.loveit.common.ThankSpecification
import com.clemble.loveit.common.model.UserID
import play.api.http.HttpEntity.Strict
import play.api.mvc.{ResponseHeader, Result}
import play.api.test.FakeRequest


class CookieUtilsSpec extends ThankSpecification {

  "READ cookie" in {
    val user = someRandom[UserID]
    val res = Result(ResponseHeader(200), Strict(ByteString.empty, None))
    val resWithCookie = CookieUtils.setUser(res, user)

    val userCookie = resWithCookie.header.headers.toSeq.head._2
    val cookie = userCookie.substring(7, userCookie.indexOf(";"))

    val req = FakeRequest().withHeaders("Cookie" -> s"userID=$cookie;")
    val readUser = CookieUtils.readUser(req)

    readUser shouldEqual Some(user)
  }

}
