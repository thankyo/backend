package com.clemble.thank.model.error

import com.clemble.thank.model.SerializationSpec
import com.clemble.thank.test.util.{Generator, RepositoryExceptionGenerator}
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner
import play.api.libs.json.Format

@RunWith(classOf[JUnitRunner])
class RepositoryExceptionSerializationSpec extends SerializationSpec[RepositoryException] {

  override val generator: Generator[RepositoryException] = RepositoryExceptionGenerator
  override val jsonFormat: Format[RepositoryException] = ThankException.repoExcJsonFormat

}
