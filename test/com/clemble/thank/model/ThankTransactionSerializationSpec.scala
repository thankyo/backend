package com.clemble.thank.model

import com.clemble.thank.test.util.{Generator, ThankTransactionGenerator}
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner
import play.api.libs.json.Format

@RunWith(classOf[JUnitRunner])
class ThankTransactionSerializationSpec extends SerializationSpec[ThankTransaction] {

  override val generator: Generator[ThankTransaction] = ThankTransactionGenerator
  override val jsonFormat: Format[ThankTransaction] = ThankTransaction.jsonFormat

}
