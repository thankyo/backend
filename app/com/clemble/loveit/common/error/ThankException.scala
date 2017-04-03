package com.clemble.loveit.common.error

import com.clemble.loveit.common.util.WriteableUtils
import play.api.libs.json._

sealed trait ThankException extends RuntimeException

case class RepositoryException(errors: Seq[RepositoryError]) extends ThankException {
  def this(error: RepositoryError) = this(Seq(error))
}

case class UserException(code: String, message: String) extends ThankException

object UserException {
  def notEnoughFunds() = new UserException("NOT_ENOUGH_FUNDS", "Not enough funds")
}

case class PaymentException(code: String, message: String) extends ThankException {
}

object PaymentException {
  def withdrawFailure() = PaymentException("WITHDRAW_FAILURE", "System does not respond try later")
}


object ThankException {
  implicit val repoExcJsonFormat = Json.format[RepositoryException]
  implicit val userExcJsonFormat = Json.format[UserException]
  implicit val paymentExcJsonFormat = Json.format[PaymentException]

  implicit val jsonFormat = new Format[ThankException] {

    val REPO = JsString("repo")
    val USER = JsString("user")
    val PAYMENT = JsString("payment")

    override def reads(json: JsValue): JsResult[ThankException] = (json \ "type") match {
      case JsDefined(REPO) => repoExcJsonFormat.reads(json)
      case JsDefined(USER) => userExcJsonFormat.reads(json)
      case JsDefined(PAYMENT) => paymentExcJsonFormat.reads(json)
      case _ => JsError(s"Can't parse ${json}")
    }

    override def writes(o: ThankException): JsValue = o match {
      case re: RepositoryException => repoExcJsonFormat.writes(re).as[JsObject] + ("type" -> REPO)
      case ue: UserException => userExcJsonFormat.writes(ue).as[JsObject] + ("type" -> USER)
      case pe: PaymentException => paymentExcJsonFormat.writes(pe).as[JsObject] + ("type" -> PAYMENT)
    }

  }

  implicit val thankExceptionWriteable = WriteableUtils.jsonToWriteable[ThankException]

}