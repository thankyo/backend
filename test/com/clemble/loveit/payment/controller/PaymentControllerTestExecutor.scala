package com.clemble.loveit.payment.controller


import com.clemble.loveit.common.ControllerSpec
import com.clemble.loveit.common.model._
import com.clemble.loveit.payment.PaymentTestExecutor
import com.clemble.loveit.payment.model.{ChargeAccount, EOMCharge, EOMPayout, PendingTransaction, StripeCustomerToken}
import com.clemble.loveit.payment.service.PendingTransactionService
import com.clemble.loveit.payment.service.repository.PaymentLimitExecutor
import com.clemble.loveit.thank.model.Project
import com.clemble.loveit.thank.service.repository.ProjectRepository
import play.api.libs.json.{JsString, Json}
import play.api.test.FakeRequest

import com.clemble.loveit.dev.service.DevStripeUtils._

trait PaymentControllerTestExecutor extends ControllerSpec with PaymentTestExecutor with PaymentLimitExecutor {

  val thankService = dependency[PendingTransactionService]
  val projectRepo = dependency[ProjectRepository]

  override def charges(user: UserID): Seq[EOMCharge] = {
    val res = perform(user, FakeRequest(GET, s"/api/v1/payment/my/charge"))
    res.body.dataStream.readJson[Seq[EOMCharge]].get
  }

  override def payouts(user: UserID): Seq[EOMPayout] = {
    val res = perform(user, FakeRequest(GET, s"/api/v1/payment/my/payout"))
    val payouts = res.body.dataStream.readJson[Seq[EOMPayout]]
    payouts.get
  }

  override def getChargeAccount(user: UserID): Option[ChargeAccount] = {
    val req = sign(user, FakeRequest(GET, s"/api/v1/payment/my/charge/account"))
    val fRes = route(application, req).get

    val res = await(fRes)
    res.header.status match {
      case 200 => res.body.dataStream.readJson[ChargeAccount]
      case _ => None
    }
  }

  override def addChargeAccount(user: UserID, token: StripeCustomerToken = someValidStripeToken()): ChargeAccount = {
    val req = FakeRequest(POST, s"/api/v1/payment/my/charge/account").
      withJsonBody(JsString(token))
    val res = perform(user, req)
    res.body.dataStream.readJson[ChargeAccount].get
  }

  override def getMonthlyLimit(user: UserID): Option[Money] = {
    val req = sign(user, FakeRequest(GET, s"/api/v1/payment/my/charge/limit"))
    val fRes = route(application, req).get

    val res = await(fRes)
    res.header.status match {
      case 200 => res.body.dataStream.readJson[Money]
      case 404 => None
    }
  }

  override def setMonthlyLimit(user: UserID, limit: Money): Boolean = {
    val req = sign(user, FakeRequest(POST, s"/api/v1/payment/my/charge/limit").withJsonBody(Json.toJson(limit)))
    val res = await(route(application, req).get)

    res.body.dataStream.readJson[Money].isDefined
  }

  override def thank(giver: UserID, project: Project, resource: Resource): PendingTransaction = {
    await(thankService.create(giver, project, resource))
  }

}
