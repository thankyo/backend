package com.clemble.thank.model.error

import com.clemble.thank.model.SerializationSpec
import com.clemble.thank.test.util.{Generator, ThankExceptionGenerator}
import play.api.libs.json.Format

class ThankExceptionSerializationSpec extends SerializationSpec[ThankException] {

  override val generator: Generator[ThankException] = ThankExceptionGenerator
  override val jsonFormat: Format[ThankException] = ThankException.jsonFormat

}
