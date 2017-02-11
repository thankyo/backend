package com.clemble.thank.model.error

import com.clemble.thank.model.SerializationSpec
import com.clemble.thank.test.util.{Generator, RepositoryExceptionGenerator}
import play.api.libs.json.Format

class RepositoryExceptionSerializationSpec extends SerializationSpec[RepositoryException] {

  override val generator: Generator[RepositoryException] = RepositoryExceptionGenerator
  override val jsonFormat: Format[RepositoryException] = RepositoryException.jsonFormat

}
