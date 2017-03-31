package com.clemble.loveit.payment.model

import com.clemble.loveit.model.SerializationSpec
import com.clemble.loveit.test.util.{Generator, PaymentTransactionGenerator}
import play.api.libs.json.Format

class PaymentTransactionSerializationSpec extends SerializationSpec[PaymentTransaction] {

  override val generator: Generator[PaymentTransaction] = PaymentTransactionGenerator
  override val jsonFormat: Format[PaymentTransaction] = PaymentTransaction.jsonFormat

}
