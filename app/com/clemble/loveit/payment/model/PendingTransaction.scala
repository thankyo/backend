package com.clemble.loveit.payment.model

import java.time.LocalDateTime

import com.clemble.loveit.common.model.{CreatedAware, Project, ProjectAware, ProjectPointer, Resource, ResourceAware, ThankEvent, UserAware}
import com.clemble.loveit.common.util.WriteableUtils
import play.api.http.Writeable
import play.api.libs.json.Json

case class PendingTransaction(
  project: ProjectPointer,
  url: Resource,
  created: LocalDateTime = LocalDateTime.now()
) extends CreatedAware with UserAware with ResourceAware with ProjectAware {

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
