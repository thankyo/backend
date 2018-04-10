package com.clemble.loveit.common.error
import com.clemble.loveit.common.model.{Money, Resource, UserID}
import com.clemble.loveit.common.util.WriteableUtils
import play.api.http.Writeable
import play.api.libs.json._

sealed trait ThankException extends RuntimeException

case class FieldValidationError(field: String, error: String) extends ThankException

object FieldValidationError {

  def validatePassword(password: String) = {
    if (password.length > 65) throw FieldValidationError("password", "Max size is 64 characters")
  }

}

case class RepositoryException(code: String, message: String) extends RuntimeException(message) with ThankException
case class UserException(code: String, message: String) extends RuntimeException(message) with ThankException
case class PaymentException(code: String, message: String) extends ThankException
case class SelfLovingForbiddenException(message: String) extends ThankException
case class ResourceException(code: String, message: String) extends ThankException

object UserException {
  def notEnoughFunds() = new UserException("NOT_ENOUGH_FUNDS", "Not enough funds")
  def urlAlreadyOwned(user: UserID) = new UserException("URL_ALREADY_OWNED", s"Resource already owned by ${user}")
}

object PaymentException {
  def alreadyThanked(user: UserID, url: Resource) = PaymentException("ALREADY_THANKED", s"User ${user} already thanked ${url}")
  def failedToLinkChargeAccount(user: UserID) = PaymentException("BANK_DETAILS_LINK", s"User ${user} failed to link charge account details")
  def failedToLinkPayoutAccount(user: UserID) = PaymentException("BANK_DETAILS_LINK", s"User ${user} failed to link payout account details")
  def limitIsNegative(user: UserID, limit: Money) = PaymentException("LIMIT_IS_NEGATIVE", s"User ${user} limit can't be negative")
  def selfLovingIsForbidden() = SelfLovingForbiddenException("No self loving allowed around here")
}

object ResourceException {
  val OWNER_MISSING_CODE = "OWNER_MISSING_CODE"
  val DIFFERENT_OWNER_CODE = "DIFFERENT_OWNER_CODE"
  val DIFFERENT_ID_CODE = "DIFFERENT_ID_CODE"
  val NO_RESOURCE_EXISTS_CODE = "NO_RESOURCE_EXISTS_CODE"

  def noResourceExists() = ResourceException(NO_RESOURCE_EXISTS_CODE, "URL was not registered")
  def failedToUpdate() = ResourceException("FAILED_TO_UPDATE", "Failed to update the record")
  def projectAlreadyCreated() = ResourceException("PROJECT_ALREADY_CREATED", "Project with specified URL already exists")
  def ownerMissing(url: Resource) = ResourceException(OWNER_MISSING_CODE, s"No owner for the resource registered for ${url}")
  def ownershipNotVerified() = ResourceException("OWNERSHIP_NOT_VERIFIED", "Can't verify ownership of resource")
  def differentOwner() = ResourceException(DIFFERENT_OWNER_CODE, "Different owner registered for the project")
  def differentId() = ResourceException(DIFFERENT_ID_CODE, "Different project id")
}

object RepositoryException {
  val UNKNOWN_CODE = "0"
  val UNKNOWN_MESSAGE = "unknown"

  val DUPLICATE_KEY_CODE = "11000"
  val DUPLICATE_KEY_MESSAGE = "Duplicate key"

  def duplicateKey(message: String = DUPLICATE_KEY_MESSAGE): RepositoryException = {
    RepositoryException(DUPLICATE_KEY_CODE, message)
  }

  def unknown() = RepositoryException("UNKNOWN", "Unknown error")
  def failedToUpdate() = RepositoryException("Failed to update", "Unknown server error")
}

object ThankException {
  implicit val repoExcJsonFormat = Json.format[RepositoryException]
  implicit val userExcJsonFormat = Json.format[UserException]
  implicit val paymentExcJsonFormat = Json.format[PaymentException]
  implicit val resourceExcJsonFormat = Json.format[ResourceException]
  implicit val fieldValidationErrorFormat = Json.format[FieldValidationError]

  implicit val jsonFormat = new Format[ThankException] {

    val REPO = JsString("repo")
    val USER = JsString("user")
    val PAYMENT = JsString("payment")
    val RESOURCE = JsString("resource")
    val FIELD = JsString("field")

    override def reads(json: JsValue): JsResult[ThankException] = (json \ "type").toOption collect {
      case REPO => repoExcJsonFormat.reads(json)
      case USER => userExcJsonFormat.reads(json)
      case PAYMENT => paymentExcJsonFormat.reads(json)
      case RESOURCE => resourceExcJsonFormat.reads(json)
      case FIELD => fieldValidationErrorFormat.reads(json)
    } getOrElse JsError(s"Can't parse ${json}")

    override def writes(o: ThankException): JsValue = o match {
      case re: RepositoryException => repoExcJsonFormat.writes(re) + ("type" -> REPO)
      case ue: UserException => userExcJsonFormat.writes(ue) + ("type" -> USER)
      case pe: PaymentException => paymentExcJsonFormat.writes(pe) + ("type" -> PAYMENT)
      case re: ResourceException => resourceExcJsonFormat.writes(re) + ("type" -> RESOURCE)
      case fv: FieldValidationError => fieldValidationErrorFormat.writes(fv) + ("type" -> FIELD)
    }

  }

  implicit val thankExceptionWriteable: Writeable[ThankException] = WriteableUtils.jsonToWriteable[ThankException]

}