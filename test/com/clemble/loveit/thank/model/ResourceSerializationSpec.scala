package com.clemble.loveit.thank.model

import com.clemble.loveit.common.SerializationSpec
import com.clemble.loveit.common.model.Resource
import com.clemble.loveit.test.util.{Generator, ResourceGenerator}
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner
import play.api.libs.json.Format

@RunWith(classOf[JUnitRunner])
class ResourceSerializationSpec extends SerializationSpec[Resource] {

  override val generator: Generator[Resource] = ResourceGenerator
  override val jsonFormat: Format[Resource] = Resource.jsonFormat

}
