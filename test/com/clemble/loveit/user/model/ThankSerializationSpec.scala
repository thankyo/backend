package com.clemble.loveit.user.model

import com.clemble.loveit.common.model.HttpResource
import com.clemble.loveit.test.util.{Generator, ThankGenerator}
import com.clemble.loveit.thank.model.Thank
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner
import play.api.libs.json.Format

@RunWith(classOf[JUnitRunner])
class ThankSerializationSpec extends SerializationSpec[Thank] {

  override val generator: Generator[Thank] = ThankGenerator
  override val jsonFormat: Format[Thank] = Thank.jsonFormat

  "with parents" should {

    "return all" in {
      val thank = Thank(HttpResource("example.com/some/what"))
      val expected = List(
        Thank(HttpResource("example.com/some/what")),
        Thank(HttpResource("example.com/some")),
        Thank(HttpResource("example.com"))
      )
      thank.withParents() shouldEqual expected
    }

  }
}
