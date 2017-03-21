package com.clemble.thank

import akka.util.ByteString
import com.clemble.thank.model._
import com.clemble.thank.model.error.ThankException
import com.clemble.thank.payment.model.PaymentTransaction
import play.api.http.{ContentTypes, Writeable}
import play.api.libs.json.Format

package object controller {

  implicit def jsonToWriteable[T]()(implicit jsonFormat: Format[T]) = new Writeable[T]((ownership: T) => {
    val json = jsonFormat.writes(ownership)
    ByteString(json.toString())
  }, Some(ContentTypes.JSON))

  implicit val thankExceptionWriteable = jsonToWriteable[ThankException]
  implicit val paymentTransactionWriteable = ControllerSafeUtils.jsonToWriteable[PaymentTransaction]
  implicit val thankTransactionWriteable = ControllerSafeUtils.jsonToWriteable[ThankTransaction]
  implicit val resourceOwnershipWriteable = ControllerSafeUtils.jsonToWriteable[ResourceOwnership]
  implicit val userWriteable = ControllerSafeUtils.jsonToWriteable[User]
  implicit val thankWriteable = ControllerSafeUtils.jsonToWriteable[Thank]

}
