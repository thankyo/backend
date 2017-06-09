package com.clemble.loveit.payment.model

import com.clemble.loveit.common.model._
import com.clemble.loveit.common.util.{WriteableUtils}
import com.clemble.loveit.user.model.UserAware
import org.joda.time.DateTime
import play.api.libs.json._

case class ThankTransaction(
                             user: UserID,
                             destination: UserID,
                             resource: Resource,
                             created: DateTime = DateTime.now()
                  ) extends ResourceAware with Transaction

object ThankTransaction {

  /**
    * JSON format for [[ThankTransaction]]
    */
  implicit val jsonFormat = Json.format[ThankTransaction]

  implicit val thankTransactionWriteable = WriteableUtils.jsonToWriteable[ThankTransaction]

}