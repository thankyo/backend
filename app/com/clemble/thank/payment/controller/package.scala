package com.clemble.thank.payment

import com.clemble.thank.payment.model.PaymentTransaction

package object controller {

  implicit val paymentTransactionWriteable = com.clemble.thank.controller.jsonToWriteable[PaymentTransaction]

}
