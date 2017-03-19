package com.clemble.thank.model

import com.clemble.thank.payment.model.PaymentTransaction
import com.clemble.thank.test.util.{Generator, PaymentTransactionGenerator}
import play.api.libs.json.Format

class PaymentTransactionSerializationSpec extends SerializationSpec[PaymentTransaction] {

  override val generator: Generator[PaymentTransaction] = PaymentTransactionGenerator
  override val jsonFormat: Format[PaymentTransaction] = PaymentTransaction.jsonFormat

}
