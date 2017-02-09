package com.clemble.thank.model
import com.clemble.thank.test.util.{BankDetailsGenerator, Generator}
import play.api.libs.json.Format

class BankDetailsSerializationSpec extends SerializationSpec[BankDetails] {

  override val generator: Generator[BankDetails] = BankDetailsGenerator
  override val jsonFormat: Format[BankDetails] = BankDetails.jsonFormat

}
