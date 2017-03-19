package com.clemble.thank.payment.model

import com.clemble.thank.model.{CreatedAware, UserAware}

trait Transaction extends CreatedAware with UserAware {
  val operation: PaymentOperation
}
