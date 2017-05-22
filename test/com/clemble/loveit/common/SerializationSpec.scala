package com.clemble.loveit.common

import com.clemble.loveit.test.util.Generator
import org.specs2.mutable.Specification
import play.api.libs.json.{Format, JsValue, Json}

import scala.util.Try

trait SerializationSpec[T] extends ThankSpecification {

  val generator: Generator[T]
  val jsonFormat: Format[T]

  "JSON" should {
    val value = generator.generate()

    "serialize" in {
      Try(Json.toJson(value)(jsonFormat)) must (beSuccessfulTry[JsValue])
    }

    "deserialize" in {
      val presentation = Json.toJson(value)(jsonFormat)
      val readValue = Json.fromJson[T](presentation)(jsonFormat).asOpt
      readValue must (beEqualTo(Some(value)))
    }
  }

}
