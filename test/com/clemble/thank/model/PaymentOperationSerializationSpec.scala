package com.clemble.thank.model
import com.clemble.thank.test.util.{Generator, PaymentOperationGenerator}
import play.api.libs.json.Format

class PaymentOperationSerializationSpec extends SerializationSpec[PaymentOperation] {

  override val generator: Generator[PaymentOperation] = PaymentOperationGenerator
  override val jsonFormat: Format[PaymentOperation] = PaymentOperation.jsonFormat

}
