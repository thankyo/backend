package com.clemble.thank.controller

import akka.stream.scaladsl.Source
import akka.util.ByteString
import com.clemble.thank.model.error.ThankException
import play.api.http.{ContentTypes, HttpChunk, HttpEntity, Writeable}
import play.api.libs.json.Format
import play.api.mvc.{Result, Results}

import scala.concurrent.{ExecutionContext, Future}

object ControllerSafeUtils extends Results {

  implicit val thankExceptionWriteable = jsonToWriteable[ThankException]

  implicit def jsonToWriteable[T]()(implicit jsonFormat: Format[T]) = new Writeable[T]((ownership: T) => {
    val json = jsonFormat.writes(ownership)
    ByteString(json.toString())
  }, Some(ContentTypes.JSON))

  def ok[T](source: Source[T, _])(implicit ec: ExecutionContext, writeable: Writeable[T]): Result = {
    val httpSource: Source[HttpChunk, _] = source.map[HttpChunk](c => HttpChunk.Chunk(writeable.transform(c)))
    Result(
      header = Ok.header,
      body = HttpEntity.Chunked(httpSource, writeable.contentType)
    )
  }

  def ok[T](f: Future[T])(implicit ec: ExecutionContext, writes: Writeable[T]): Future[Result] = {
    val jsonResult = f.map(res => Ok(res))
    safe(jsonResult)
  }

  def okOrNotFound[T](f: Future[Option[T]])(implicit ec: ExecutionContext, writeable: Writeable[T]): Future[Result] = {
    val jsonResult = f.map(_ match {
      case Some(t) => Ok(t)
      case None => NotFound
    })
    safe(jsonResult)
  }

  def created[T](f: Future[T])(implicit ec: ExecutionContext, writes: Writeable[T]): Future[Result] = {
    val jsonResult = f.map(res => Created(res))
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
