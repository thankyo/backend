package com.clemble.thank.payment

import com.clemble.thank.controller.ControllerSafeUtils
import com.clemble.thank.payment.model.PaymentTransaction

package object controller {

  implicit val paymentTransactionWriteable = ControllerSafeUtils.jsonToWriteable[PaymentTransaction]

}
