package com.clemble.thank.service.repository.mongo

import com.clemble.thank.model.error.{RepositoryError, RepositoryException}
import reactivemongo.api.commands.{WriteError, WriteResult}
import reactivemongo.core.errors.DatabaseException

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

  def safe[T](success:() => T, fTask: Future[WriteResult])(implicit ec: ExecutionContext): Future[T] = {
    val fTranslated = fTask.flatMap(res => {
      if (res.ok && res.n == 1) {
        Future.successful(success())
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
    })
  }

}
