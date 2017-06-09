package com.clemble.loveit.payment.controller

import com.clemble.loveit.common.ControllerSpec
import com.clemble.loveit.common.model.UserID
import com.clemble.loveit.payment.model.{Money}
import play.api.libs.json.{Json}
import play.api.test.FakeRequest

class MonthlyLimitControllerSpec extends ControllerSpec {

  def getMonthlyLimit(user: UserID): Option[Money] = {
    val req = sign(user, FakeRequest(GET, s"/api/v1/payment/limit/month/my"))
    val fRes = route(application, req).get

    val res = await(fRes)
    res.header.status match {
      case 200 => Json.parse(res.body.dataStream.read()).asOpt[Money]
      case 404 => None
    }
  }

  def setMonthlyLimit(user: UserID, limit: Money): Money = {
    val req = sign(user, FakeRequest(POST, s"/api/v1/payment/limit/month/my").withJsonBody(Json.toJson(limit)))
    val res = await(route(application, req).get)

    Json.parse(res.body.dataStream.read()).as[Money]
  }


  "Update Limit" in {
    val user = createUser()
    val limit = someRandom[Money]

    val limitBefore = getMonthlyLimit(user)
    val updatedLimit = Some(setMonthlyLimit(user, limit))

    val limitAfter = getMonthlyLimit(user)

    limitAfter shouldEqual updatedLimit
    limitBefore shouldNotEqual limitAfter
  }



}
