package com.clemble.loveit.payment

import com.clemble.loveit.common.controller.jsonToWriteable
import com.clemble.loveit.payment.model.{PaymentTransaction, ThankTransaction}

package object controller {

  implicit val paymentTransactionWriteable = jsonToWriteable[PaymentTransaction]
  implicit val thankTransactionWriteable = jsonToWriteable[ThankTransaction]

}
