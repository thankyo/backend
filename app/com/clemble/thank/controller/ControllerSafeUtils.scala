package com.clemble.thank.controller

import com.clemble.thank.model.error.ThankException
import play.api.libs.iteratee.Enumerator
import play.api.libs.json.{Json, Writes}
import play.api.mvc.{Result, Results}

import scala.concurrent.{ExecutionContext, Future}

/**
  * Created by mavarazy on 2/12/17.
  */
object ControllerSafeUtils extends Results {

  def okChunked[T](fEnum: Future[Enumerator[T]])(implicit ec: ExecutionContext, writes: Writes[T]): Future[Result] = {
    val jsonResult = fEnum.map(enum => {
      val jsonPaymentSource = enum.map(payment => Json.toJson(payment))
      Ok.chunked(jsonPaymentSource)
    })
    safe(jsonResult)
  }

  def ok[T](f: Future[T])(implicit ec: ExecutionContext, writes: Writes[T]): Future[Result] = {
    val jsonResult = f.map(res => Ok(Json.toJson(res)))
    safe(jsonResult)
  }

  def okOrNotFound[T](f: Future[Option[T]])(implicit ec: ExecutionContext, writes: Writes[T]): Future[Result] = {
    val jsonResult = f.map(_ match {
      case Some(t) => Ok(Json.toJson(t))
      case None => NotFound
    })
    safe(jsonResult)
  }

  def created[T](f: Future[T])(implicit ec: ExecutionContext, writes: Writes[T]): Future[Result] = {
    val jsonResult = f.map(res => Created(Json.toJson(res)))
    safe(jsonResult)
  }

  def safe(f: Future[Result])(implicit ec: ExecutionContext): Future[Result] = {
    f.recover({
      case re: ThankException =>
        BadRequest(Json.toJson(re))
      case t: Throwable =>
        InternalServerError(t.getMessage)
    })
  }
}
