package com.clemble.loveit.common.model

import java.time.LocalDateTime

import com.clemble.loveit.common.util.WriteableUtils
import com.clemble.loveit.user.model.UserAware
import play.api.http.Writeable
import play.api.libs.json._

case class ThankTransaction(
                             user: UserID,
                             destination: UserID,
                             resource: Resource,
                             created: LocalDateTime = LocalDateTime.now()
                  ) extends ResourceAware with CreatedAware with UserAware

object ThankTransaction {

  /**
    * JSON format for [[ThankTransaction]]
    */
  implicit val jsonFormat: OFormat[ThankTransaction] = Json.format[ThankTransaction]
  implicit val thankTransactionWriteable: Writeable[ThankTransaction] = WriteableUtils.jsonToWriteable[ThankTransaction]

}