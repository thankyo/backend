package com.clemble.loveit.payment.model

import com.clemble.loveit.common.SerializationSpec
import com.clemble.loveit.test.util.{BankDetailsGenerator, Generator}
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner
import play.api.libs.json.Format

@RunWith(classOf[JUnitRunner])
class BankDetailsSerializationSpec extends SerializationSpec[BankDetails] {

  override val generator: Generator[BankDetails] = BankDetailsGenerator
  override val jsonFormat: Format[BankDetails] = BankDetails.jsonFormat

}
