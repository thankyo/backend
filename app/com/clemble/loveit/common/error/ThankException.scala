package com.clemble.loveit.common.error
import com.clemble.loveit.common.model.{Resource, UserID}
import com.clemble.loveit.common.util.WriteableUtils
import com.clemble.loveit.payment.model.Money
import play.api.libs.json._

sealed trait ThankException extends RuntimeException

case class RepositoryException(code: String, message: String) extends RuntimeException(message) with ThankException
case class UserException(code: String, message: String) extends RuntimeException(message) with ThankException
case class PaymentException(code: String, message: String) extends ThankException
case class ResourceException(code: String, message: String) extends ThankException

object UserException {
  def notEnoughFunds() = new UserException("NOT_ENOUGH_FUNDS", "Not enough funds")
  def resourceAlreadyOwned(user: UserID) = new UserException("RESOURCE_ALREADY_OWNED", s"Resource already owned by ${user}")
}

object PaymentException {
  def alreadyThanked(user: UserID, res: Resource) = PaymentException("ALREADY_THANKED", s"User ${user} already thanked ${res}")
  def failedToLinkChargeAccount(user: UserID) = PaymentException("BANK_DETAILS_LINK", s"User ${user} failed to link bank details")
  def limitIsNegative(user: UserID, limit: Money) = PaymentException("LIMIT_IS_NEGATIVE", s"User ${user} limit can't be negative")
}

object ResourceException {
  val OWNER_MISSING_CODE = "OWNER_MISSING_CODE"

  def verificationAlreadyRequested() = ResourceException("VERIFICATION_IN_PROGRESS", "Resource verification already in progress")
  def ownerMissing() = new ResourceException(OWNER_MISSING_CODE, "No owner for the resource registered")
}

object RepositoryException {
  val UNKNOWN_CODE = "0"
  val UNKNOWN_MESSAGE = "unknown"

  val DUPLICATE_KEY_CODE = "11000"
  val DUPLICATE_KEY_MESSAGE = "Duplicate key"

  def duplicateKey(message: String = DUPLICATE_KEY_MESSAGE) = {
    RepositoryException(DUPLICATE_KEY_CODE, message)
  }

  def unknown() = RepositoryException("UNKNOWN", "Unknown error")

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