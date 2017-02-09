package com.clemble.thank.model
import com.clemble.thank.test.util.{Generator, ThankGenerator}
import play.api.libs.json.Format


class ThankSerializationSpec extends SerializationSpec[Thank] {

  override val generator: Generator[Thank] = ThankGenerator
  override val jsonFormat: Format[Thank] = Thank.jsonFormat

}
