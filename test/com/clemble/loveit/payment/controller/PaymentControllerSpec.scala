package com.clemble.loveit.payment.controller

import java.time.YearMonth

import com.clemble.loveit.common.ControllerSpec
import com.clemble.loveit.common.model.{Resource, UserID}
import com.clemble.loveit.payment.model.{ChargeAccount, EOMCharge, EOMPayout, EOMStatus, Money, StripeCustomerToken, ThankTransaction}
import com.clemble.loveit.payment.service.{TestStripeUtils, ThankTransactionService}
import play.api.libs.json.{JsString, Json}
import play.api.test.FakeRequest

trait PaymentControllerSpec extends ControllerSpec with TestStripeUtils {
  val thankService = dependency[ThankTransactionService]

  def charges(user: UserID): Seq[EOMCharge] = {
    val res = perform(user, FakeRequest(GET, s"/api/v1/payment/charge/my"))
    val charges = res.body.dataStream.map(byteStream => Json.parse(byteStream.utf8String).as[EOMCharge])
    charges.toSeq()
  }

  def payouts(user: UserID): Seq[EOMPayout] = {
    val res = perform(user, FakeRequest(GET, s"/api/v1/payment/payout/my"))
    val charges = res.body.dataStream.map(byteStream => Json.parse(byteStream.utf8String).as[EOMPayout])
    charges.toSeq()
  }

  def getChargeAccount(user: UserID): Option[ChargeAccount] = {
    val req = sign(user, FakeRequest(GET, s"/api/v1/payment/bank/my"))
    val fRes = route(application, req).get

    val res = await(fRes)
    res.header.status match {
      case 200 => res.body.dataStream.readJson[ChargeAccount]
      case 404 => None
    }
  }

  def addChargeAccount(user: UserID, token: StripeCustomerToken = someValidStripeToken()): ChargeAccount = {
    val req = FakeRequest(POST, s"/api/v1/payment/charge/my/account").
      withJsonBody(JsString(token))
    val res = perform(user, req)
    res.body.dataStream.readJson[ChargeAccount].get
  }

  def getMonthlyLimit(user: UserID): Option[Money] = {
    val req = sign(user, FakeRequest(GET, s"/api/v1/payment/limit/month/my"))
    val fRes = route(application, req).get

    val res = await(fRes)
    res.header.status match {
      case 200 => res.body.dataStream.readJson[Money]
      case 404 => None
    }
  }

  def setMonthlyLimit(user: UserID, limit: Money): Money = {
    val req = sign(user, FakeRequest(POST, s"/api/v1/payment/limit/month/my").withJsonBody(Json.toJson(limit)))
    val res = await(route(application, req).get)

    res.body.dataStream.readJson[Money].get
  }

  def thank(giver: UserID, owner: UserID, resource: Resource): ThankTransaction = {
    await(thankService.create(giver, owner, resource))
  }

  def pendingThanks(giver: UserID): Seq[ThankTransaction] = {
    val res = perform(giver, FakeRequest(GET, s"/api/v1/payment/pending/my"))
    res.body.dataStream.map(str => Json.parse(str.utf8String).as[ThankTransaction]).toSeq()
  }

}
