package com.clemble.loveit.payment.controller


import com.clemble.loveit.common.ControllerSpec
import com.clemble.loveit.common.model.{Resource, UserID}
import com.clemble.loveit.payment.PaymentTestExecutor
import com.clemble.loveit.payment.model.{ChargeAccount, EOMCharge, EOMPayout, EOMStatus, Money, StripeCustomerToken, ThankTransaction}
import com.clemble.loveit.payment.service.ThankTransactionService
import play.api.libs.json.{JsString, Json}
import play.api.test.FakeRequest

trait PaymentControllerTestExecutor extends ControllerSpec with PaymentTestExecutor {

  val thankService = dependency[ThankTransactionService]

  override def charges(user: UserID): Seq[EOMCharge] = {
    val res = perform(user, FakeRequest(GET, s"/api/v1/payment/my/charge"))
    val charges = res.body.dataStream.map(byteStream => Json.parse(byteStream.utf8String).as[EOMCharge])
    charges.toSeq()
  }

  override def payouts(user: UserID): Seq[EOMPayout] = {
    val res = perform(user, FakeRequest(GET, s"/api/v1/payment/my/payout"))
    val charges = res.body.dataStream.map(byteStream => Json.parse(byteStream.utf8String).as[EOMPayout])
    charges.toSeq()
  }

  override def getChargeAccount(user: UserID): Option[ChargeAccount] = {
    val req = sign(user, FakeRequest(GET, s"/api/v1/payment/my/account"))
    val fRes = route(application, req).get

    val res = await(fRes)
    res.header.status match {
      case 200 => res.body.dataStream.readJson[ChargeAccount]
      case 404 => None
    }
  }

  override def addChargeAccount(user: UserID, token: StripeCustomerToken = someValidStripeToken()): ChargeAccount = {
    val req = FakeRequest(POST, s"/api/v1/payment/my/account").
      withJsonBody(JsString(token))
    val res = perform(user, req)
    res.body.dataStream.readJson[ChargeAccount].get
  }

  override def getMonthlyLimit(user: UserID): Option[Money] = {
    val req = sign(user, FakeRequest(GET, s"/api/v1/payment/my/limit"))
    val fRes = route(application, req).get

    val res = await(fRes)
    res.header.status match {
      case 200 => res.body.dataStream.readJson[Money]
      case 404 => None
    }
  }

  override def setMonthlyLimit(user: UserID, limit: Money): Money = {
    val req = sign(user, FakeRequest(POST, s"/api/v1/payment/my/limit").withJsonBody(Json.toJson(limit)))
    val res = await(route(application, req).get)

    res.body.dataStream.readJson[Money].get
  }

  override def thank(giver: UserID, owner: UserID, resource: Resource): ThankTransaction = {
    await(thankService.create(giver, owner, resource))
  }

  override def pendingThanks(giver: UserID): Seq[ThankTransaction] = {
    val res = perform(giver, FakeRequest(GET, s"/api/v1/payment/my/pending"))
    val pending = res.body.consumeData.map(str => Json.parse(str.utf8String).as[List[ThankTransaction]])
    await(pending)
  }

}
