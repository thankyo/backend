package com.clemble.loveit.model.error

import play.api.libs.json.Json

case class RepositoryError(code: String, message: String) {

  override def equals(obj: scala.Any): Boolean = {
    obj match {
      case RepositoryError(otherCode, _) => otherCode == code
      case _ => false
    }
  }

}

object RepositoryError {

  implicit val jsonFormat = Json.format[RepositoryError]

  val UNKNOWN_CODE = "0"
  val UNKNOWN_MESSAGE = "unknown"

  val DUPLICATE_KEY_CODE = "11000"
  val DUPLICATE_KEY_MESSAGE = "Duplicate key"

  def duplicateKey(message: String = DUPLICATE_KEY_MESSAGE) = {
    RepositoryError(DUPLICATE_KEY_CODE, message)
  }

}