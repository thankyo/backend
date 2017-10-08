package com.clemble.loveit.common

import com.clemble.loveit.test.util.Generator
import play.api.libs.json.{Format, JsValue, Json}

import scala.util.Try

abstract class SerializationSpec[T](implicit jsonFormat: Format[T], generator: Generator[T]) extends ThankSpecification {

  "JSON" should {
    val value: T = someRandom[T]

    "serialize" in {
      Try(Json.toJson(value)(jsonFormat)) must (beSuccessfulTry[JsValue])
    }

    "deserialize" in {
      val presentation = Json.toJson(value)(jsonFormat)
      val readValue = Json.fromJson[T](presentation)(jsonFormat).asOpt
      readValue must (beSome(value))
    }
  }

  def jsonSerialization[T]()(implicit jsonFormat: Format[T], generator: Generator[T]) = {
    "JSON" should {
      val value: T = someRandom[T]

      "serialize" in {
        Try(Json.toJson(value)(jsonFormat)) must (beSuccessfulTry[JsValue])
      }

      "deserialize" in {
        val presentation = Json.toJson(value)(jsonFormat)
        val readValue = Json.fromJson[T](presentation)(jsonFormat).asOpt
        readValue must (beSome(value))
      }
    }
  }

}
