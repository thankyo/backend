package com.clemble.loveit.payment.model

import java.time.LocalDateTime

import com.clemble.loveit.common.model.{CreatedAware, Resource, ResourceAware, ThankEvent}
import com.clemble.loveit.common.util.WriteableUtils
import com.clemble.loveit.thank.model.Project
import com.clemble.loveit.user.model.UserAware
import play.api.http.Writeable
import play.api.libs.json.Json

case class PendingTransaction(
  project: Project,
  url: Resource,
  created: LocalDateTime = LocalDateTime.now()
) extends CreatedAware with UserAware with ResourceAware {

  val user = project.user

}

object PendingTransaction {

  def from(thank: ThankEvent): PendingTransaction = {
    PendingTransaction(thank.project, thank.url, thank.created)
  }

  implicit val jsonFormat = Json.format[PendingTransaction]
  implicit val pendingTransactionWriteable: Writeable[PendingTransaction] = WriteableUtils.jsonToWriteable[PendingTransaction]
  implicit val pendingTransactionListWriteable: Writeable[List[PendingTransaction]] = WriteableUtils.jsonToWriteable[List[PendingTransaction]]

}
