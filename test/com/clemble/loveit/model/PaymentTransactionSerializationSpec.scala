package com.clemble.loveit.model

import com.clemble.loveit.payment.model.PaymentTransaction
import com.clemble.loveit.test.util.{Generator, PaymentTransactionGenerator}
import play.api.libs.json.Format

class PaymentTransactionSerializationSpec extends SerializationSpec[PaymentTransaction] {

  override val generator: Generator[PaymentTransaction] = PaymentTransactionGenerator
  override val jsonFormat: Format[PaymentTransaction] = PaymentTransaction.jsonFormat

}
