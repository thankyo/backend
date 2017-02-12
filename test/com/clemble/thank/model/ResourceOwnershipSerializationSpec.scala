package com.clemble.thank.model

import com.clemble.thank.test.util.{Generator, ResourceOwnershipGenerator}
import play.api.libs.json.Format

class ResourceOwnershipSerializationSpec extends SerializationSpec[ResourceOwnership] {

  override val generator: Generator[ResourceOwnership] = ResourceOwnershipGenerator
  override val jsonFormat: Format[ResourceOwnership] = ResourceOwnership.jsonFormat

}
