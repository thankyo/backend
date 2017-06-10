package com.clemble.loveit.payment.controller

import java.time.YearMonth

import com.clemble.loveit.common.ControllerSpec
import com.clemble.loveit.payment.model.{EOMStatus}
import com.clemble.loveit.payment.service.GenericEOMServiceSpec
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner
import play.api.libs.json.Json
import play.api.test.FakeRequest

@RunWith(classOf[JUnitRunner])
class AdminEOMControllerSpec extends GenericEOMServiceSpec with ControllerSpec {

  val admin = createUser()

  override def getStatus(yom: YearMonth): Option[EOMStatus] = {
    val res = perform(admin, FakeRequest(GET, s"/api/v1/payment/admin/eom/${yom.getYear}/${yom.getMonthValue}"))
    res.header.status match {
      case 200 => Json.parse(res.body.dataStream.read()).asOpt[EOMStatus]
      case 404 => None
    }
  }

  override def run(yom: YearMonth): EOMStatus = {
    val res = perform(admin, FakeRequest(POST, s"/api/v1/payment/admin/eom/${yom.getYear}/${yom.getMonthValue}"))
    Json.parse(res.body.dataStream.read()).as[EOMStatus]
  }


}
