package com.clemble.thank.model

import com.clemble.thank.test.util.{Generator, PaymentGenerator}
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner
import play.api.libs.json.Format

@RunWith(classOf[JUnitRunner])
class PaymentSerializationSpec extends SerializationSpec[Payment] {

  override val generator: Generator[Payment] = PaymentGenerator
  override val jsonFormat: Format[Payment] = Payment.jsonFormat

}
