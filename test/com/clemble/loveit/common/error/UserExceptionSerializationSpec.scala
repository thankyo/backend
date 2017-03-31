package com.clemble.loveit.common.error

import com.clemble.loveit.model.SerializationSpec
import com.clemble.loveit.test.util.{Generator, UserExceptionGenerator}
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner
import play.api.libs.json.Format

@RunWith(classOf[JUnitRunner])
class UserExceptionSerializationSpec extends SerializationSpec[UserException] {

  override val generator: Generator[UserException] = UserExceptionGenerator
  override val jsonFormat: Format[UserException] = ThankException.userExcJsonFormat

}
