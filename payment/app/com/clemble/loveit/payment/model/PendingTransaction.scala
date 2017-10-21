package com.clemble.loveit.payment.model

import java.time.LocalDateTime

import com.clemble.loveit.common.model.{CreatedAware, Resource, ThankEvent}
import com.clemble.loveit.common.util.WriteableUtils
import com.clemble.loveit.thank.model.SupportedProject
import com.clemble.loveit.user.model.UserAware
import play.api.http.Writeable
import play.api.libs.json.Json

case class PendingTransaction(
                               project: SupportedProject,
                               resource: Resource,
                               created: LocalDateTime = LocalDateTime.now()
                             ) extends CreatedAware with UserAware {

  val user = project.user

}

object PendingTransaction {

  def from(thank: ThankEvent): PendingTransaction = {
    PendingTransaction(thank.project, thank.resource, thank.created)
  }

  implicit val jsonFormat = Json.format[PendingTransaction]
  implicit val thankTransactionWriteable: Writeable[PendingTransaction] = WriteableUtils.jsonToWriteable[PendingTransaction]

}
