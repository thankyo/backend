package com.clemble.thank.model

import com.clemble.thank.test.util.{Generator, ResourceOwnershipGenerator}
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner
import play.api.libs.json.Format

@RunWith(classOf[JUnitRunner])
class ResourceOwnershipSerializationSpec extends SerializationSpec[ResourceOwnership] {

  override val generator: Generator[ResourceOwnership] = ResourceOwnershipGenerator
  override val jsonFormat: Format[ResourceOwnership] = ResourceOwnership.jsonFormat

}
