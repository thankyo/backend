package com.clemble.loveit.common.mongo

import akka.stream.Materializer
import com.clemble.loveit.common.error.{RepositoryError, RepositoryException, ThankException}
import play.api.libs.json.JsObject
import reactivemongo.akkastream.cursorProducer
import reactivemongo.api.ReadPreference
import reactivemongo.api.commands.{WriteError, WriteResult}
import reactivemongo.api.indexes.Index
import reactivemongo.core.errors.DatabaseException
import reactivemongo.play.json._
import reactivemongo.play.json.collection.JSONCollection

import scala.concurrent.{ExecutionContext, Future}

object MongoSafeUtils {

  def toError(code: Int, msg: String) = {
    code match {
      case 11000 => RepositoryError.duplicateKey(msg)
      case _ => RepositoryError(code.toString(), msg)
    }
  }

  def toException(writeErrors: Seq[WriteError]): RepositoryException = {
    val errors = writeErrors.map(err => toError(err.code, err.errmsg))
    new RepositoryException(errors)
  }

  def toException(dbExc: DatabaseException): RepositoryException = {
    val err = toError(
      dbExc.code.getOrElse(-1),
      dbExc.message
    )
    new RepositoryException(err)
  }

  def safe[T](success: => T, fTask: Future[WriteResult])(implicit ec: ExecutionContext): Future[T] = {
    val fTranslated = fTask.flatMap(res => {
      if (res.ok && res.n == 1) {
        Future.successful(success)
      } else {
        val exception = MongoSafeUtils.toException(res.writeErrors)
        Future.failed(exception)
      }
    })
    safe(fTranslated)
  }

  def safe[T](fTask: Future[T])(implicit ec: ExecutionContext): Future[T] = {
    fTask.recoverWith({
      case dbExc: DatabaseException =>
        val exception = toException(dbExc)
        Future.failed(exception)
      case thExc: ThankException =>
        Future.failed(thExc)
    })
  }

  def safeSingleUpdate[T <: WriteResult](fTask: Future[T])(implicit ec: ExecutionContext): Future[Boolean] = {
    fTask.
      map(res => res.ok && res.n == 1).
      recoverWith({
        case dbExc: DatabaseException =>
          val exception = toException(dbExc)
          Future.failed(exception)
        case thExc: ThankException =>
          Future.failed(thExc)
      })
  }

  def ensureIndexes(collection: JSONCollection, indexes: Index*)(implicit ec: ExecutionContext): Unit = {
    for {
      index <- indexes
    } {
      collection.indexesManager.ensure(index).onFailure({ case t => t.printStackTrace(System.err) })
    }
  }

  def ensureUpdate(collection: JSONCollection, selector: JsObject, update: (JsObject) => Future[WriteResult]) (implicit ec: ExecutionContext, m: Materializer): Unit = {
    val source = collection.find(selector).cursor[JsObject](ReadPreference.nearest).documentSource()
    val updateEach = source.runFoldAsync(true)((agg, jsObj) => update(jsObj).map(res => agg && res.ok && res.n == 1))
    updateEach.foreach(success => if(!success) System.exit(1))
  }

}
