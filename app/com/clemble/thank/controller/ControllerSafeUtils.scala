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

  def okOrNotFound[T](f: Future[Option[T]])(implicit ec: ExecutionContext, writeable: Writeable[T]): Future[Result] = {
    val jsonResult = f.map(_ match {
      case Some(t) => Ok(t)
      case None => NotFound
    })
    safe(jsonResult)
  }

  def safe(f: Future[Result])(implicit ec: ExecutionContext): Future[Result] = {
    f.recover({
      case re: ThankException =>
        BadRequest(re)
      case t: Throwable =>
        InternalServerError(t.getMessage)
    })
  }
}
