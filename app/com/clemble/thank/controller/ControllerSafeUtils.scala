package com.clemble.thank.controller

import akka.util.ByteString
import com.clemble.thank.model.error.ThankException
import play.api.http.{ContentTypes, Writeable}
import play.api.libs.json.Format
import play.api.mvc.{Result, Results}

import scala.concurrent.{ExecutionContext, Future}

object ControllerSafeUtils extends Results {

  implicit val thankExceptionWriteable = jsonToWriteable[ThankException]

  implicit def jsonToWriteable[T]()(implicit jsonFormat: Format[T]) = new Writeable[T]((ownership: T) => {
    val json = jsonFormat.writes(ownership)
    ByteString(json.toString())
  }, Some(ContentTypes.JSON))

}
