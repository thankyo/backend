package com.clemble.loveit.common.error

import com.clemble.loveit.common.SerializationSpec
import com.clemble.loveit.test.util.{Generator, ThankExceptionGenerator}
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner
import play.api.libs.json.Format

@RunWith(classOf[JUnitRunner])
class ThankExceptionSerializationSpec extends SerializationSpec[ThankException]
