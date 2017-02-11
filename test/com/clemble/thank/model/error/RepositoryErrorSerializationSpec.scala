package com.clemble.thank.model.error

import com.clemble.thank.model.SerializationSpec
import com.clemble.thank.test.util.{Generator, RepositoryErrorGenerator}
import play.api.libs.json.Format

class RepositoryErrorSerializationSpec extends SerializationSpec[RepositoryError] {
  override val generator: Generator[RepositoryError] = RepositoryErrorGenerator
  override val jsonFormat: Format[RepositoryError] = RepositoryError.jsonFormat
}
