package com.clemble.loveit.payment.model

import java.time.{LocalDateTime, YearMonth}

import com.clemble.loveit.common.model.{Money, UserID}
import com.clemble.loveit.common.util.{LoveItCurrency, WriteableUtils}
import com.clemble.loveit.payment.model.ChargeStatus.ChargeStatus
import play.api.http.Writeable
import play.api.libs.json._
import com.clemble.loveit.common.model.yearMonthJsonFormat

object ChargeStatus extends Enumeration {
  type ChargeStatus = Value
  val Pending, Running, Success, Failed, UnderMin, NoBankDetails, FailedToCreate = Value

  val MIN_CHARGE = Money(5.0, LoveItCurrency.getInstance("USD"))

  def isUnderMin(amount: Money): Boolean = {
    amount < MIN_CHARGE
  }

  implicit val jsonFormat: Format[ChargeStatus] = new Format[ChargeStatus] {
    override def writes(o: ChargeStatus): JsValue = {
      JsString(o.toString)
    }

    override def reads(json: JsValue): JsResult[ChargeStatus] = json match {
      case JsString(str) => JsSuccess(ChargeStatus.withName(str))
      case _ => JsError(s"Not ChargeStatus $json")
    }
  }
}

case class EOMCharge(
                      user: UserID,
                      yom: YearMonth,
                      account: Option[ChargeAccount],
                      status: ChargeStatus,
                      amount: Money,
                      details: Option[JsValue],
                      transactions: List[PendingTransaction],
                      created: LocalDateTime = LocalDateTime.now()
) extends Transaction with EOMAware

object EOMCharge {

  implicit val jsonFormat: OFormat[EOMCharge] = Json.format[EOMCharge]
  implicit val chargeWriteable: Writeable[EOMCharge] = WriteableUtils.jsonToWriteable[EOMCharge]
  implicit val writeableJsonCollection: Writeable[List[EOMCharge]] = WriteableUtils.jsonToWriteable[List[EOMCharge]]

}


