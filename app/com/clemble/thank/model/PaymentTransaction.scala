package com.clemble.thank.model

import org.joda.time.{DateTime, DateTimeZone}
import play.api.libs.json._
import com.braintreegateway.{Transaction => BraintreeTransaction}
import reactivemongo.bson.BSONObjectID

sealed trait PaymentStatus
case object Pending extends PaymentStatus
case object Complete extends PaymentStatus
case object Error extends PaymentStatus

object PaymentStatus {

  implicit val jsonFormat = new Format[PaymentStatus] {
    val PENDING_JSON = JsString("pending")
    val COMPLETE_JSON = JsString("complete")
    val ERROR_JSON = JsString("error")

    override def writes(o: PaymentStatus): JsValue = o match {
      case Pending => PENDING_JSON
      case Complete => COMPLETE_JSON
      case Error => ERROR_JSON
    }

    override def reads(json: JsValue): JsResult[PaymentStatus] = json match {
      case PENDING_JSON => JsSuccess(Pending)
      case COMPLETE_JSON => JsSuccess(Complete)
      case ERROR_JSON => JsSuccess(Error)
      case _ => JsError(s"Failed to read ${json} as PaymentStatus")
    }
  }

}

case class PaymentTransaction(
                               id: PaymentID,
                               operation: PaymentOperation,
                               user: UserID,
                               thanks: Amount,
                               money: Money,
                               source: BankDetails,
                               destination: BankDetails,
                               status: PaymentStatus,
                               created: DateTime = DateTime.now(DateTimeZone.UTC)
                             ) extends Transaction

object PaymentTransaction {

  implicit val jsonFormat = Json.format[PaymentTransaction]

  def debit(user: UserID, thanks: Amount, money: Money, source: BankDetails): PaymentTransaction = {
    PaymentTransaction(
      id = BSONObjectID.generate().stringify,
      operation = Debit,
      user = user,
      thanks = thanks,
      money = money,
      source = source,
      destination = EmptyBankDetails,
      status = Complete,
      created = DateTime.now(DateTimeZone.UTC)
    )
  }


}


