package com.clemble.loveit.common.mongo

import akka.stream.Materializer
import akka.stream.scaladsl.Source
import com.clemble.loveit.common.error.{RepositoryException, ThankException}
import play.api.Logger
import play.api.libs.json.{JsObject, Json, Reads}
import reactivemongo.akkastream.{State, cursorProducer}
import reactivemongo.api.{Cursor, ReadPreference}
import reactivemongo.api.commands.{WriteError, WriteResult}
import reactivemongo.api.indexes.Index
import reactivemongo.core.errors.DatabaseException
import reactivemongo.play.json._
import reactivemongo.play.json.collection.JSONCollection

import scala.concurrent.{ExecutionContext, Future}

object MongoSafeUtils {

  private def ignoreErrorHandler[T](collectionName: String, query: JsObject) = {
    Cursor.ContOnError[T]((_, thr) =>
      Logger.error(s"Mongo[${collectionName}] failed to query with ${query}", thr)
    )
  }

  def findAll[T](collection: JSONCollection, selector: JsObject, projection: JsObject = Json.obj())(implicit reads: Reads[T], ec: ExecutionContext, m: Materializer): Source[T, Future[State]] = {
    collection.
      find(selector, projection).
      cursor[T](ReadPreference.nearest).
      documentSource(err = ignoreErrorHandler(collection.name, selector))
  }

  def toException(code: Int, msg: String): RepositoryException = {
    code match {
      case 11000 => RepositoryException.duplicateKey(msg)
      case _ => RepositoryException(code.toString, msg)
    }
  }

  def toException(writeErrors: Seq[WriteError]): RepositoryException = {
    val exception = writeErrors.headOption.map(err => toException(err.code, err.errmsg))
    exception.getOrElse(RepositoryException.unknown())
  }

  def toException(dbExc: DatabaseException): RepositoryException = {
    toException(dbExc.code.getOrElse(-1), dbExc.message)
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
      collection.indexesManager.ensure(index).
        recover({ case t =>
          t.printStackTrace(System.err)
          System.exit(2)
        })
    }
  }

  def ensureUpdate(collection: JSONCollection, selector: JsObject, update: (JsObject) => Future[WriteResult])(implicit ec: ExecutionContext, m: Materializer): Unit = {
    val source = collection.find(selector).cursor[JsObject](ReadPreference.nearest).documentSource()
    val updateEach = source.runFoldAsync(true)((agg, jsObj) => update(jsObj).map(res => agg && res.ok && res.n == 1))
    updateEach.foreach(success => if (!success) System.exit(1))
  }

}
