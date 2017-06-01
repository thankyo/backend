package com.clemble.loveit.thank.model

import com.clemble.loveit.common.SerializationSpec
import com.clemble.loveit.common.model.Resource
import com.clemble.loveit.test.util.{Generator, ROVerificationGenerator}
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner
import play.api.libs.json.Format

@RunWith(classOf[JUnitRunner])
class ROVerificationSerializationSpec extends SerializationSpec[ROVerification[Resource]] {

  override val generator: Generator[ROVerification[Resource]] = ROVerificationGenerator
  override val jsonFormat: Format[ROVerification[Resource]] = ROVerification.jsonFormat

}
