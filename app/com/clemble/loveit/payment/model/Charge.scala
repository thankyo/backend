package com.clemble.loveit.payment.model

import com.clemble.loveit.common.model.{Amount, PaymentID, UserID}
import com.clemble.loveit.common.util.WriteableUtils
import com.clemble.loveit.payment.model.ChargeStatus.ChargeStatus
import org.joda.time.DateTime
import play.api.libs.json._

object ChargeStatus extends Enumeration {
  type ChargeStatus = Value
  val Pending, Running, Complete, Failed = Value

  implicit val jsonFormat = new Format[ChargeStatus] {
    override def writes(o: ChargeStatus): JsValue = {
      JsString(o.toString)
    }

    override def reads(json: JsValue): JsResult[ChargeStatus] = json match {
      case JsString(str) => JsSuccess(ChargeStatus.withName(str))
    }
  }
}

case class Charge(
                   id: PaymentID,
                   user: UserID,
                   source: BankDetails,
                   status: ChargeStatus,
                   money: Money,
                   transactions: List[ThankTransaction],
                   created: DateTime = DateTime.now()
) extends Transaction

object Charge {

  implicit val jsonFormat = Json.format[Charge]
  implicit val chargeWriteable = WriteableUtils.jsonToWriteable[Charge]

}


