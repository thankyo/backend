package com.clemble.loveit.model.error

import com.clemble.loveit.model.SerializationSpec
import com.clemble.loveit.test.util.{Generator, RepositoryErrorGenerator}
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner
import play.api.libs.json.Format

@RunWith(classOf[JUnitRunner])
class RepositoryErrorSerializationSpec extends SerializationSpec[RepositoryError] {

  override val generator: Generator[RepositoryError] = RepositoryErrorGenerator
  override val jsonFormat: Format[RepositoryError] = RepositoryError.jsonFormat

}
