package com.clemble.loveit.user.model

import com.clemble.loveit.common.SerializationSpec
import com.clemble.loveit.test.util.{Generator, UserGenerator}
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner
import play.api.libs.json.Format

@RunWith(classOf[JUnitRunner])
class UserSerializationSpec extends SerializationSpec[User]
