package com.clemble.loveit.payment.controller

import com.clemble.loveit.common.ControllerSpec
import com.clemble.loveit.common.model.UserID
import com.clemble.loveit.payment.model.{ChargeAccount, StripeCustomerToken, ThankTransaction}
import com.clemble.loveit.payment.service.TestStripeUtils
import play.api.libs.json.{JsString, Json}
import play.api.test.FakeRequest

class ChargeAccountControllerSpec extends ControllerSpec with TestStripeUtils {

  def getChargeAccount(user: UserID): Option[ChargeAccount] = {
    val req = sign(user, FakeRequest(GET, s"/api/v1/payment/bank/my"))
    val fRes = route(application, req).get

    val res = await(fRes)
    res.header.status match {
      case 200 => res.body.dataStream.readJson[ChargeAccount]
      case 404 => None
    }
  }

  def setChargeAccount(user: UserID, token: StripeCustomerToken): ChargeAccount = {
    val req = sign(user, FakeRequest(POST, s"/api/v1/payment/bank/my").withJsonBody(JsString(token)))
    val res = await(route(application, req).get)

    res.body.dataStream.readJson[ChargeAccount].get
  }

  "Update ChargeAccount" in {
    val user = createUser()

    val chAccBefore = getChargeAccount(user)
    val updatedChAcc = Some(setChargeAccount(user, someValidStripeToken()))

    val chAccAfter = getChargeAccount(user)

    chAccAfter shouldEqual updatedChAcc
    chAccBefore shouldNotEqual chAccAfter
  }

}
