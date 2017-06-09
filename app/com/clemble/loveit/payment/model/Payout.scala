package com.clemble.loveit.payment.model

import com.clemble.loveit.common.model.{CreatedAware, UserID}
import com.clemble.loveit.payment.model.PayoutStatus.PayoutStatus
import com.clemble.loveit.user.model.UserAware
import org.joda.time.DateTime
import play.api.libs.json._

object PayoutStatus extends Enumeration {
  type PayoutStatus = Value
  val Pending, Running, Complete, Failed = Value

  implicit val jsonFormat = new Format[PayoutStatus] {
    override def writes(o: PayoutStatus): JsValue = {
      JsString(o.toString)
    }

    override def reads(json: JsValue): JsResult[PayoutStatus] = json match {
      case JsString(str) => JsSuccess(PayoutStatus.withName(str))
    }
  }
}

case class Payout(
                   id: String,
                   user: UserID,
                   bankDetails: BankDetails,
                   failed: Long,
                   pending: Long,
                   status: PayoutStatus,
                   created: DateTime = new DateTime()
) extends Transaction

object Payout {

  implicit val jsonFormat = Json.format[Payout]

}