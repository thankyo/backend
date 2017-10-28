package com.clemble.loveit.user.controller

import com.clemble.loveit.auth.model.AuthResponse
import com.clemble.loveit.auth.model.requests.RegisterRequest
import com.clemble.loveit.common.ControllerSpec
import org.junit.runner.RunWith
import org.specs2.concurrent.ExecutionEnv
import org.specs2.runner.JUnitRunner
import play.api.libs.json.Json
import play.api.test.FakeRequest

@RunWith(classOf[JUnitRunner])
class AuthResponseControllerSpec(implicit ee: ExecutionEnv) extends ControllerSpec {

  "existing false on new user" in {
    val profile = someRandom[RegisterRequest]

    val req = FakeRequest(POST, "/api/v1/auth/register").withJsonBody(Json.toJson(profile))
    val fRes = route(application, req).get.flatMap(_.body.consumeData).map(_.utf8String)

    val authResp = Json.parse(await(fRes)).as[AuthResponse]

    authResp.existing shouldEqual false
  }

  "existing true on log In" in {
    val profile = someRandom[RegisterRequest]

    val creteReq = FakeRequest(POST, "/api/v1/auth/register").withJsonBody(Json.toJson(profile))
    val createResp = await(route(application, creteReq).get.flatMap(_.body.consumeData).map(_.utf8String).map(str => Json.parse(str).as[AuthResponse]))

    createResp.existing shouldEqual false

    val loginReq = FakeRequest(POST, "/api/v1/auth/logIn").withJsonBody(Json.toJson(profile.toLogIn()))
    val loginResp = await(route(application, loginReq).get.flatMap(_.body.consumeData).map(_.utf8String).map(str => Json.parse(str).as[AuthResponse]))

    loginResp.existing shouldEqual true
  }

}

