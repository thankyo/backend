package com.clemble.thank.model

trait Transaction extends CreatedAware with UserAware {
  val operation: PaymentOperation
}
