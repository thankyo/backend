package com.clemble.loveit.user.model

import com.clemble.loveit.test.util.{Generator, UserGenerator}
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner
import play.api.libs.json.Format

@RunWith(classOf[JUnitRunner])
class UserSerializationSpec extends SerializationSpec[User] {

  override val generator: Generator[User] = UserGenerator
  override val jsonFormat: Format[User] = User.jsonFormat

}
