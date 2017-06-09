package com.clemble.loveit.payment.controller

import com.clemble.loveit.common.ControllerSpec
import com.clemble.loveit.common.model.UserID
import com.clemble.loveit.payment.model.{BankDetails, StripeCustomerToken, ThankTransaction}
import com.clemble.loveit.payment.service.TestStripeUtils
import play.api.libs.json.{JsString, Json}
import play.api.test.FakeRequest

class BankDetailsControllerSpec extends ControllerSpec with TestStripeUtils {

  def getBankDetails(user: UserID): Option[BankDetails] = {
    val req = sign(user, FakeRequest(GET, s"/api/v1/payment/bank/my"))
    val fRes = route(application, req).get

    val res = await(fRes)
    res.header.status match {
      case 200 => Json.parse(res.body.dataStream.read()).asOpt[BankDetails]
      case 404 => None
    }
  }

  def setBankDetails(user: UserID, token: StripeCustomerToken): BankDetails = {
    val req = sign(user, FakeRequest(POST, s"/api/v1/payment/bank/my").withJsonBody(JsString(token)))
    val res = await(route(application, req).get)

    Json.parse(res.body.dataStream.read()).as[BankDetails]
  }

  "Update BankDetails" in {
    val user = createUser()

    val bankDetailsBefore = getBankDetails(user)
    val updatedBankDetails = Some(setBankDetails(user, someValidStripeToken()))

    val bankDetailsAfter = getBankDetails(user)

    bankDetailsAfter shouldEqual updatedBankDetails
    bankDetailsBefore shouldNotEqual bankDetailsAfter
  }

}
