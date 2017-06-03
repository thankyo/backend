package com.clemble.loveit.common.error

import com.clemble.loveit.common.model.{Resource, UserID}
import com.clemble.loveit.common.util.WriteableUtils
import com.clemble.loveit.user.model.User
import play.api.libs.json._

sealed trait ThankException extends RuntimeException

case class RepositoryException(errors: Seq[RepositoryError]) extends RuntimeException(errors.head.message) with ThankException {
  def this(error: RepositoryError) = this(Seq(error))
}

case class UserException(code: String, message: String) extends RuntimeException(message) with ThankException

object UserException {
  def notEnoughFunds() = new UserException("NOT_ENOUGH_FUNDS", "Not enough funds")
  def resourceAlreadyOwned(user: UserID) = new UserException("RESOURCE_ALREADY_OWNED", s"Resource already owned by ${user}")
  def resourceOwnershipImpossible() = new UserException("RESOURCE_OWNERSHIP_IMPOSSIBLE", "Resource can't be owned")
  def userMissing(userID: UserID) = new UserException("USER_MISSING", s"Can't find user ${userID}")
}

case class PaymentException(code: String, message: String) extends ThankException
object PaymentException {
  def withdrawFailure() = PaymentException("WITHDRAW_FAILURE", "System does not respond try later")
  def alreadyThanked(user: UserID, res: Resource) = PaymentException("ALREADY_THANKED", s"User ${user} already thanked ${res}")
}


case class ResourceException(code: String, message: String) extends ThankException
object ResourceException {
  def verificationAlreadyRequested() = ResourceException("VERIFICATION_IN_PROGRESS", "Resource verification already in progress")
}

object ThankException {
  implicit val repoExcJsonFormat = Json.format[RepositoryException]
  implicit val userExcJsonFormat = Json.format[UserException]
  implicit val paymentExcJsonFormat = Json.format[PaymentException]
  implicit val resourceExcJsonFormat = Json.format[ResourceException]

  implicit val jsonFormat = new Format[ThankException] {

    val REPO = JsString("repo")
    val USER = JsString("user")
    val PAYMENT = JsString("payment")
    val RESOURCE = JsString("resource")

    override def reads(json: JsValue): JsResult[ThankException] = (json \ "type") match {
      case JsDefined(REPO) => repoExcJsonFormat.reads(json)
      case JsDefined(USER) => userExcJsonFormat.reads(json)
      case JsDefined(PAYMENT) => paymentExcJsonFormat.reads(json)
      case JsDefined(RESOURCE) => resourceExcJsonFormat.reads(json)
      case _ => JsError(s"Can't parse ${json}")
    }

    override def writes(o: ThankException): JsValue = o match {
      case re: RepositoryException => repoExcJsonFormat.writes(re).as[JsObject] + ("type" -> REPO)
      case ue: UserException => userExcJsonFormat.writes(ue).as[JsObject] + ("type" -> USER)
      case pe: PaymentException => paymentExcJsonFormat.writes(pe).as[JsObject] + ("type" -> PAYMENT)
      case re: ResourceException => resourceExcJsonFormat.writes(re).as[JsObject] + ("type" -> RESOURCE)
    }

  }

  implicit val thankExceptionWriteable = WriteableUtils.jsonToWriteable[ThankException]

}