package com.clemble.loveit.common.mongo

import akka.stream.Materializer
import akka.stream.scaladsl.Source
import com.clemble.loveit.common.error.{RepositoryException, ThankException}
import play.api.Logger
import play.api.libs.json.{JsObject, Json, Reads}
import reactivemongo.akkastream.{State, cursorProducer}
import reactivemongo.api.commands.{WriteError, WriteResult}
import reactivemongo.api.indexes.Index
import reactivemongo.api.{Cursor, ReadPreference}
import reactivemongo.core.errors.DatabaseException
import reactivemongo.play.json._
import reactivemongo.play.json.collection.JSONCollection

import scala.concurrent.{Await, ExecutionContext, Future}
import scala.concurrent.duration._

object MongoSafeUtils {

  private def ignoreErrorHandler[T](collectionName: String, query: JsObject) = {
    Cursor.ContOnError[T]((_, thr) =>
      Logger.error(s"Mongo[$collectionName] failed to query with ${query}", thr)
    )
  }

  def findAll[T](collection: JSONCollection, selector: JsObject, projection: JsObject = Json.obj(), sort: JsObject = Json.obj())(implicit reads: Reads[T], ec: ExecutionContext, m: Materializer): Source[T, Future[State]] = {
    collection.
      find(selector, projection).
      sort(sort).
      cursor[T](ReadPreference.nearest).
      documentSource(err = ignoreErrorHandler(collection.name, selector))
  }

  def collectAll[T](collection: JSONCollection, selector: JsObject, projection: JsObject = Json.obj(), sort: JsObject = Json.obj())(implicit reads: Reads[T], ec: ExecutionContext, m: Materializer): Future[List[T]] = {
    val fAll = findAll[T](collection, selector, projection, sort)
    fAll
      .runFold(List.empty[T])((l, el) => el :: l)
      .map(_.reverse)
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
    val fEnsuredIndexes = indexes.map(collection.indexesManager.ensure(_))
    Await.result(Future.sequence(fEnsuredIndexes), 1 minute)
  }

}
