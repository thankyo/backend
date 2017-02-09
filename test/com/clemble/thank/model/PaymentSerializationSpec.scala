package com.clemble.thank.model
import com.clemble.thank.test.util.{Generator, PaymentGenerator}
import play.api.libs.json.Format

class PaymentSerializationSpec extends SerializationSpec[Payment] {

  override val generator: Generator[Payment] = PaymentGenerator
  override val jsonFormat: Format[Payment] = Payment.jsonFormat

}
