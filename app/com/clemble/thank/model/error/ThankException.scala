package com.clemble.thank.model.error

import play.api.libs.json.Json

sealed trait ThankException extends RuntimeException
case class RepositoryException(errors: Seq[RepositoryError]) extends ThankException {
  def this(error: RepositoryError) = this(Seq(error))
}

object RepositoryException {
  implicit val jsonFormat = Json.format[RepositoryException]
}