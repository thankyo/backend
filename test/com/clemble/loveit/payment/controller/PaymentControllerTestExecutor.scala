package com.clemble.loveit.payment.controller


import com.clemble.loveit.common.ControllerSpec
import com.clemble.loveit.common.model.{Project, _}
import com.clemble.loveit.payment.PaymentTestExecutor
import com.clemble.loveit.payment.model.{ChargeAccount, EOMCharge, EOMPayout, PendingTransaction, StripeCustomerToken}
import com.clemble.loveit.payment.service.PendingTransactionService
import com.clemble.loveit.payment.service.repository.PaymentLimitExecutor
import com.clemble.loveit.thank.service.repository.ProjectRepository
import play.api.libs.json.{JsString, Json}
import play.api.test.FakeRequest
import com.clemble.loveit.dev.service.DevStripeUtils._

trait PaymentControllerTestExecutor extends ControllerSpec with PaymentTestExecutor with PaymentLimitExecutor {

  val thankService = dependency[PendingTransactionService]
  val projectRepo = dependency[ProjectRepository]

  override def charges(user: UserID): Seq[EOMCharge] = {
    val res = perform(user, FakeRequest(GET, s"/api/v1/payment/my/charge"))
    res.body.dataStream.readJson[Seq[EOMCharge]]
  }

  override def payouts(user: UserID): Seq[EOMPayout] = {
    val res = perform(user, FakeRequest(GET, s"/api/v1/payment/my/payout"))
    val payouts = res.body.dataStream.readJson[Seq[EOMPayout]]
    payouts
  }

  override def getChargeAccount(user: UserID): Option[ChargeAccount] = {
    val res = perform(user, FakeRequest(GET, s"/api/v1/payment/my/charge/account"))
    res.header.status match {
      case 200 => res.body.dataStream.readJsonOpt[ChargeAccount]
      case _ => None
    }
  }

  override def addChargeAccount(user: UserID, token: StripeCustomerToken = someValidStripeToken()): ChargeAccount = {
    val req = FakeRequest(POST, s"/api/v1/payment/my/charge/account").
      withJsonBody(JsString(token))
    val res = perform(user, req)
    res.body.dataStream.readJson[ChargeAccount]
  }

  override def getMonthlyLimit(user: UserID): Option[Money] = {
    val res = perform(user, FakeRequest(GET, s"/api/v1/payment/my/charge/limit"))
    res.header.status match {
      case 200 => res.body.dataStream.readJsonOpt[Money]
      case 404 => None
    }
  }

  override def setMonthlyLimit(user: UserID, limit: Money): Boolean = {
    val res = perform(user, FakeRequest(POST, s"/api/v1/payment/my/charge/limit").withJsonBody(Json.toJson(limit)))

    res.body.dataStream.readJsonOpt[Money].isDefined
  }

  override def thank(giver: UserID, project: Project, url: Resource): PendingTransaction = {
    await(thankService.create(giver, project, url))
  }

}
