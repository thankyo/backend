package com.clemble.thank.model.error

import play.api.libs.json._

sealed trait ThankException extends RuntimeException
case class RepositoryException(errors: Seq[RepositoryError]) extends ThankException {
  def this(error: RepositoryError) = this(Seq(error))
}

object RepositoryException {
  implicit val jsonFormat = Json.format[RepositoryException]
}

object ThankException {

  implicit val jsonFormat = new Format[ThankException] {

    val REPO = JsString("repo")

    override def reads(json: JsValue): JsResult[ThankException] = (json \ "type") match {
      case JsDefined(REPO) => RepositoryException.jsonFormat.reads(json)
      case _ => JsError(s"Can't parse ${json}")
    }

    override def writes(o: ThankException): JsValue = o match {
      case re: RepositoryException => RepositoryException.jsonFormat.writes(re).as[JsObject] + ("type" -> REPO)
    }

  }

}