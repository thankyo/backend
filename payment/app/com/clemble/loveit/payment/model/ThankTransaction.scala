package com.clemble.loveit.payment.model

import java.time.LocalDateTime

import com.clemble.loveit.common.model._
import com.clemble.loveit.common.util.WriteableUtils
import play.api.http.Writeable
import play.api.libs.json._

case class ThankTransaction(
                             user: UserID,
                             destination: UserID,
                             resource: Resource,
                             created: LocalDateTime = LocalDateTime.now()
                  ) extends ResourceAware with Transaction

object ThankTransaction {

  /**
    * JSON format for [[ThankTransaction]]
    */
  implicit val jsonFormat: OFormat[ThankTransaction] = Json.format[ThankTransaction]
  implicit val thankTransactionWriteable: Writeable[ThankTransaction] = WriteableUtils.jsonToWriteable[ThankTransaction]

}