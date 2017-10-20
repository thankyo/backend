package com.clemble.loveit.payment.model

import java.time.LocalDateTime

import com.clemble.loveit.common.model.{Resource, ThankTransaction, UserID}
import com.clemble.loveit.common.util.WriteableUtils
import play.api.http.Writeable
import play.api.libs.json.Json

case class PendingTransaction(
                               destination: UserID,
                               resource: Resource,
                               created: LocalDateTime = LocalDateTime.now()
                             )

object PendingTransaction {

  def from(thank: ThankTransaction): PendingTransaction = {
    PendingTransaction(thank.destination, thank.resource, thank.created)
  }

  implicit val jsonFormat = Json.format[PendingTransaction]
  implicit val thankTransactionWriteable: Writeable[PendingTransaction] = WriteableUtils.jsonToWriteable[PendingTransaction]

}
