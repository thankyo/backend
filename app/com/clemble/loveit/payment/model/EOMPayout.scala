package com.clemble.loveit.payment.model

import com.clemble.loveit.common.model.UserID
import com.clemble.loveit.payment.model.PayoutStatus.PayoutStatus
import org.joda.time.DateTime
import play.api.libs.json._

case object PayoutStatus extends Enumeration {
  type PayoutStatus = Value
  val Pending, Running, Success, Failed = Value

  implicit val jsonFormat = new Format[PayoutStatus] {
    override def writes(o: PayoutStatus): JsValue = {
      JsString(o.toString)
    }

    override def reads(json: JsValue): JsResult[PayoutStatus] = json match {
      case JsString(str) => JsSuccess(PayoutStatus.withName(str))
    }
  }
}

case class EOMPayout(
                      user: UserID,
                      bankDetails: BankDetails,
                      failed: Long,
                      pending: Long,
                      amount: Money,
                      status: PayoutStatus,
                      created: DateTime = new DateTime()
) extends Transaction

object EOMPayout {

  implicit val jsonFormat = Json.format[EOMPayout]

}