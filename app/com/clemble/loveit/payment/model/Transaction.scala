package com.clemble.loveit.payment.model

import com.clemble.loveit.model.{CreatedAware, UserAware}

trait Transaction extends CreatedAware with UserAware {
  val operation: PaymentOperation
}
