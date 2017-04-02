package com.clemble.loveit.payment.model

import com.clemble.loveit.common.model._
import com.clemble.loveit.common.util.IDGenerator
import org.joda.time.DateTime
import play.api.libs.json._

case class ThankTransaction(
                             id: PaymentID,
                             user: UserID,
                             amount: Amount,
                             resource: Resource,
                             operation: PaymentOperation,
                             created: DateTime = DateTime.now()
                  ) extends Transaction with ResourceAware

object ThankTransaction {

  /**
    * JSON format for [[ThankTransaction]]
    */
  implicit val jsonFormat = Json.format[ThankTransaction]

  def debit(user: UserID, uri: Resource, amount: Amount): ThankTransaction = {
    ThankTransaction(
      IDGenerator.generate(),
      user,
      amount,
      uri,
      Debit
    )
  }

  def credit(user: UserID, uri: Resource, amount: Amount): ThankTransaction = {
    ThankTransaction(
      IDGenerator.generate(),
      user,
      amount,
      uri,
      Credit
    )
  }

}