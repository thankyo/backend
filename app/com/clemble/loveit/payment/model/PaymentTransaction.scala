package com.clemble.loveit.payment.model

import com.braintreegateway.{Transaction => BraintreeTransaction}
import com.clemble.loveit.common.model.{Amount, PaymentID, UserID}
import com.clemble.loveit.common.util.{WriteableUtils}
import org.joda.time.DateTime
import play.api.libs.json._

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
                               status: PaymentStatus,
                               created: DateTime = DateTime.now()
                             ) extends Transaction

object PaymentTransaction {

  implicit val jsonFormat = Json.format[PaymentTransaction]
  implicit val paymentTransactionWriteable = WriteableUtils.jsonToWriteable[PaymentTransaction]

  def from(user: UserID, thanks: Amount, transaction: BraintreeTransaction): PaymentTransaction = {
    PaymentTransaction(
      id = transaction.getOrderId,
      operation = Debit,
      user = user,
      money = Money from transaction,
      thanks = thanks,
      source = BankDetails.from(transaction.getCustomer),
      status = Complete,
      created = new DateTime(transaction.getCreatedAt)
    )
  }

  def debit(id: String, user: UserID, thanks: Amount, money: Money, source: BankDetails): PaymentTransaction = {
    PaymentTransaction(
      id = id,
      operation = Debit,
      user = user,
      thanks = thanks,
      money = money,
      source = source,
      status = Complete
    )
  }

  def credit(id: String, user: UserID, thanks: Amount, money: Money, credit: BankDetails): PaymentTransaction = {
    PaymentTransaction(
      id = id,
      operation = Credit,
      user = user,
      thanks = thanks,
      money = money,
      source = credit,
      status = Complete
    )
  }


}


