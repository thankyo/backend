package com.clemble.loveit.common

import akka.util.ByteString
import com.clemble.loveit.common.error.ThankException
import com.clemble.loveit.user.model._
import com.clemble.loveit.payment.model.{PaymentTransaction, ThankTransaction}
import com.clemble.loveit.thank.model.{ResourceOwnership, Thank}
import play.api.http.{ContentTypes, Writeable}
import play.api.libs.json.Format

package object controller {

  implicit def jsonToWriteable[T]()(implicit jsonFormat: Format[T]) = new Writeable[T]((ownership: T) => {
    val json = jsonFormat.writes(ownership)
    ByteString(json.toString())
  }, Some(ContentTypes.JSON))

  implicit val thankExceptionWriteable = jsonToWriteable[ThankException]

}
