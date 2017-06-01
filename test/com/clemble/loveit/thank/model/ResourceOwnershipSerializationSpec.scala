package com.clemble.loveit.thank.model

import com.clemble.loveit.common.SerializationSpec
import com.clemble.loveit.test.util.Generator
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner
import play.api.libs.json.Format

@RunWith(classOf[JUnitRunner])
class ResourceOwnershipSerializationSpec extends SerializationSpec[ResourceOwnership] {

  override val generator: Generator[ResourceOwnership] = ResourceOwnershipGenerator
  override val jsonFormat: Format[ResourceOwnership] = ResourceOwnership.jsonFormat

}
