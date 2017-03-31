package com.clemble.loveit.payment

import com.clemble.loveit.payment.model.PaymentTransaction

package object controller {

  implicit val paymentTransactionWriteable = com.clemble.loveit.controller.jsonToWriteable[PaymentTransaction]

}
