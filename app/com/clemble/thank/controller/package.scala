package com.clemble.thank

import com.clemble.thank.model._

package object controller {

  implicit val paymentTransactionWriteable = ControllerSafeUtils.jsonToWriteable[PaymentTransaction]
  implicit val thankTransactionWriteable = ControllerSafeUtils.jsonToWriteable[ThankTransaction]
  implicit val resourceOwnershipWriteable = ControllerSafeUtils.jsonToWriteable[ResourceOwnership]
  implicit val userWriteable = ControllerSafeUtils.jsonToWriteable[User]
  implicit val thankWriteable = ControllerSafeUtils.jsonToWriteable[Thank]

}
