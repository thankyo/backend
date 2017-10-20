package com.clemble.loveit.payment.controller


import com.clemble.loveit.common.ControllerSpec
import com.clemble.loveit.common.model._
import com.clemble.loveit.payment.PaymentTestExecutor
import com.clemble.loveit.payment.model.{ChargeAccount, EOMCharge, EOMPayout, PendingTransaction, StripeCustomerToken}
import com.clemble.loveit.payment.service.PendingTransactionService
import com.clemble.loveit.payment.service.repository.PaymentLimitExecutor
import play.api.libs.json.{JsString, Json}
import play.api.test.FakeRequest

trait PaymentControllerTestExecutor extends ControllerSpec with PaymentTestExecutor with PaymentLimitExecutor {

  val thankService = dependency[PendingTransactionService]

  override def getBalance(user: UserID): Amount = {
    val res = perform(user, FakeRequest(GET, s"/api/v1/payment/my/balance"))
    val amountOpt = res.body.dataStream.readJson[Amount]()
    amountOpt.get
  }

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

  override def setMonthlyLimit(user: UserID, limit: Money): Boolean = {
    val req = sign(user, FakeRequest(POST, s"/api/v1/payment/my/limit").withJsonBody(Json.toJson(limit)))
    val res = await(route(application, req).get)

    res.body.dataStream.readJson[Money].isDefined
  }

  override def thank(giver: UserID, owner: UserID, resource: Resource): PendingTransaction = {
    await(thankService.create(giver, owner, resource))
  }

  override def pendingThanks(giver: UserID): Seq[PendingTransaction] = {
    val res = perform(giver, FakeRequest(GET, s"/api/v1/payment/my/pending"))
    val pending = res.body.consumeData.map(str => Json.parse(str.utf8String).as[List[PendingTransaction]])
    await(pending)
  }

}
