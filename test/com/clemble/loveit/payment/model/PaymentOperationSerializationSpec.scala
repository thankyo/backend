package com.clemble.loveit.payment.model

import com.clemble.loveit.user.model.SerializationSpec
import com.clemble.loveit.test.util.{Generator, PaymentOperationGenerator}
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner
import play.api.libs.json.Format

@RunWith(classOf[JUnitRunner])
class PaymentOperationSerializationSpec extends SerializationSpec[PaymentOperation] {

  override val generator: Generator[PaymentOperation] = PaymentOperationGenerator
  override val jsonFormat: Format[PaymentOperation] = PaymentOperation.jsonFormat

}
