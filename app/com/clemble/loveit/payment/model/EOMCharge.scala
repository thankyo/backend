package com.clemble.loveit.payment.model

import java.time.YearMonth

import com.clemble.loveit.common.model.UserID
import com.clemble.loveit.common.util.WriteableUtils
import com.clemble.loveit.payment.model.ChargeStatus.ChargeStatus
import org.joda.time.DateTime
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
                      source: BankDetails,
                      status: ChargeStatus,
                      amount: Money,
                      details: Option[JsValue],
                      transactions: List[ThankTransaction],
                      postpones: List[ThankTransaction],
                      created: DateTime = DateTime.now()
) extends Transaction with EOMAware {

  def moreThanMinCharge = amount >= source.minCharge

}

object EOMCharge {

  // TODO This is dark magic number, should be configured appropriately
  val MIN_TRANSACTIONS = 7

  implicit val jsonFormat = Json.format[EOMCharge]
  implicit val chargeWriteable = WriteableUtils.jsonToWriteable[EOMCharge]

}


