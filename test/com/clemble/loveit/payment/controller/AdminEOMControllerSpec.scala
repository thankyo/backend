package com.clemble.loveit.payment.controller

import java.time.YearMonth

import com.clemble.loveit.common.ControllerSpec
import com.clemble.loveit.common.model.{Resource, UserID}
import com.clemble.loveit.payment.model.{ChargeAccount, EOMCharge, EOMPayout, EOMStatus, ThankTransaction}
import com.clemble.loveit.payment.service.{GenericEOMServiceSpec, TestStripeUtils, ThankTransactionService}
import play.api.libs.json.{JsString, Json}
import play.api.test.FakeRequest

class AdminEOMControllerSpec extends GenericEOMServiceSpec with PaymentControllerSpec {

  val admin = createUser()

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

}
