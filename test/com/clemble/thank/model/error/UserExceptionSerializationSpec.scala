package com.clemble.thank.model.error

import com.clemble.thank.model.SerializationSpec
import com.clemble.thank.test.util.{Generator, UserExceptionGenerator}
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner
import play.api.libs.json.Format

@RunWith(classOf[JUnitRunner])
class UserExceptionSerializationSpec extends SerializationSpec[UserException] {

  override val generator: Generator[UserException] = UserExceptionGenerator
  override val jsonFormat: Format[UserException] = ThankException.userExcJsonFormat

}
