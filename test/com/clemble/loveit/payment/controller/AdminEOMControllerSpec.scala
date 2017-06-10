package com.clemble.loveit.payment.controller

import java.time.YearMonth

import com.clemble.loveit.common.ControllerSpec
import com.clemble.loveit.common.model.{Resource, UserID}
import com.clemble.loveit.payment.model.{BankDetails, EOMCharge, EOMStatus, ThankTransaction}
import com.clemble.loveit.payment.service.{GenericEOMServiceSpec, TestStripeUtils, ThankTransactionService}
import play.api.libs.json.{JsString, Json}
import play.api.test.FakeRequest

class AdminEOMControllerSpec extends GenericEOMServiceSpec with ControllerSpec with TestStripeUtils {

  val admin = createUser()
  val thankService = dependency[ThankTransactionService]

  override def getStatus(yom: YearMonth): Option[EOMStatus] = {
    val res = perform(admin, FakeRequest(GET, s"/api/v1/payment/admin/eom/${yom.getYear}/${yom.getMonthValue}"))
    res.header.status match {
      case 200 => res.body.dataStream.readJson[EOMStatus]
      case 404 => None
    }
  }

  override def run(yom: YearMonth): EOMStatus = {
    val res = perform(admin, FakeRequest(POST, s"/api/v1/payment/admin/eom/${yom.getYear}/${yom.getMonthValue}"))
    res.body.dataStream.readJson[EOMStatus].get
  }

  override def charges(user: UserID): Seq[EOMCharge] = {
    val res = perform(admin, FakeRequest(GET, s"/api/v1/payment/charge/my"))
    val charges = res.body.dataStream.map(byteStream => Json.parse(byteStream.utf8String).as[EOMCharge])
    charges.toSeq()
  }

  override def addBankDetails(user: UserID): BankDetails = {
    val res = perform(admin, FakeRequest(POST, s"/api/v1/payment/bank/my").withJsonBody(JsString(someValidStripeToken())))
    res.body.dataStream.readJson[BankDetails].get
  }

  override def thank(giver: UserID, owner: UserID, resource: Resource): ThankTransaction = {
    await(thankService.create(giver, owner, resource))
  }

  override def pendingThanks(giver: UserID): Seq[ThankTransaction] = {
    val res = perform(giver, FakeRequest(GET, s"/api/v1/payment/pending/my"))
    res.body.dataStream.map(str => Json.parse(str.utf8String).as[ThankTransaction]).toSeq()
  }

}
