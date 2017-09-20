package com.clemble.loveit.payment.model

import java.time.{LocalDateTime, YearMonth}

import com.clemble.loveit.common.model.UserID
import com.clemble.loveit.common.util.WriteableUtils
import com.clemble.loveit.payment.model.ChargeStatus.ChargeStatus
import play.api.libs.json._

object ChargeStatus extends Enumeration {
  type ChargeStatus = Value
  val Pending, Running, Success, Failed, UnderMin = Value

  implicit val jsonFormat = new Format[ChargeStatus] {
    override def writes(o: ChargeStatus): JsValue = {
      JsString(o.toString)
    }

    override def reads(json: JsValue): JsResult[ChargeStatus] = json match {
      case JsString(str) => JsSuccess(ChargeStatus.withName(str))
    }
  }
}

case class EOMCharge(
                      user: UserID,
                      yom: YearMonth,
                      account: ChargeAccount,
                      status: ChargeStatus,
                      amount: Money,
                      details: Option[JsValue],
                      transactions: List[ThankTransaction],
                      created: LocalDateTime = LocalDateTime.now()
) extends Transaction with EOMAware

object EOMCharge {

  implicit val jsonFormat = Json.format[EOMCharge]
  implicit val chargeWriteable = WriteableUtils.jsonToWriteable[EOMCharge]

}


