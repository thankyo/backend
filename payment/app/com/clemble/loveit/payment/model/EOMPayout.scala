package com.clemble.loveit.payment.model

import java.time.{LocalDateTime, YearMonth}

import com.clemble.loveit.common.model.{Money, UserID}
import com.clemble.loveit.common.util.WriteableUtils
import com.clemble.loveit.payment.model.PayoutStatus.PayoutStatus
import play.api.http.Writeable
import play.api.libs.json._

case object PayoutStatus extends Enumeration {
  type PayoutStatus = Value
  val Pending, Running, Success, Failed, NoBankAccount, NoSuccessfulCharges = Value

  implicit val jsonFormat: Format[PayoutStatus] = new Format[PayoutStatus] {
    override def writes(o: PayoutStatus): JsValue = {
      JsString(o.toString)
    }

    override def reads(json: JsValue): JsResult[PayoutStatus] = json match {
      case JsString(str) => JsSuccess(PayoutStatus.withName(str))
      case _ => JsError(s"Not a PayoutStatus $json")
    }
  }
}

case class EOMPayout(
                      user: UserID,
                      yom: YearMonth,
                      destination: Option[PayoutAccount],
                      amount: Money,
                      status: PayoutStatus,
                      transactions: List[PendingTransaction],
                      created: LocalDateTime = LocalDateTime.now()
) extends Transaction with EOMAware

object EOMPayout {

  def empty(user: UserID, yom: YearMonth): EOMPayout = {
    EOMPayout(user, yom, None, Money.ZERO, PayoutStatus.NoSuccessfulCharges, List.empty)
  }

  implicit val jsonFormat: OFormat[EOMPayout] = Json.format[EOMPayout]
  implicit val writeableJson: Writeable[EOMPayout] = WriteableUtils.jsonToWriteable[EOMPayout]
  implicit val writeableJsonCollection: Writeable[List[EOMPayout]] = WriteableUtils.jsonToWriteable[List[EOMPayout]]

}