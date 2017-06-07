package com.clemble.loveit.common

import com.clemble.loveit.test.util.Generator
import org.joda.time.DateTimeZone
import play.api.libs.json.{Format, JsValue, Json}

import scala.util.Try

abstract class SerializationSpec[T](implicit jsonFormat: Format[T], generator: Generator[T]) extends ThankSpecification {

  DateTimeZone.setDefault(DateTimeZone.UTC)

  "JSON" should {
    val value: T = someRandom[T]

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
