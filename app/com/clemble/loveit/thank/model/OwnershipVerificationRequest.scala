package com.clemble.loveit.thank.model

import com.clemble.loveit.common.model.{Resource, UserID}
import com.clemble.loveit.common.util.WriteableUtils
import com.clemble.loveit.user.model.User
import play.api.libs.json._

/**
  * States of resource ownership verification process
  */
sealed trait OwnershipVerificationRequestStatus
case object Pending extends OwnershipVerificationRequestStatus
case object Running extends OwnershipVerificationRequestStatus
case object Verified extends OwnershipVerificationRequestStatus
case object NonVerified extends OwnershipVerificationRequestStatus

object OwnershipVerificationRequestStatus {

  implicit val jsonFormat = new Format[OwnershipVerificationRequestStatus] {
    val PENDING = JsString("pending")
    val RUNNING = JsString("running")
    val VERIFIED = JsString("verified")
    val NON_VERIFIED = JsString("notVerified")

    override def reads(json: JsValue): JsResult[OwnershipVerificationRequestStatus] = {
      json match {
        case PENDING => JsSuccess(Pending)
        case RUNNING => JsSuccess(Running)
        case VERIFIED => JsSuccess(Verified)
        case NON_VERIFIED => JsSuccess(NonVerified)
        case _ => JsError(s"Can't read ${json} as OwnershipRequestStatus")
      }
    }

    override def writes(o: OwnershipVerificationRequestStatus): JsValue = {
      o match {
        case Pending => PENDING
        case Running => RUNNING
        case Verified => VERIFIED
        case NonVerified => NON_VERIFIED
      }
    }
  }

}

/**
  * Ownership request
  *
  * @param status        current status
  * @param resource      resource in question
  * @param ownershipType type of ownership
  */
case class OwnershipVerificationRequest(
                                         status: OwnershipVerificationRequestStatus,
                                         resource: Resource,
                                         ownershipType: OwnershipType,
                                         requester: UserID,
                                         verificationCode: String
                           ) {

  def toOwnership() = ResourceOwnership(resource, ownershipType)

}

object OwnershipVerificationRequest {

  implicit val jsonFormat = Json.format[OwnershipVerificationRequest]
  implicit val httpWriteable = WriteableUtils.jsonToWriteable[OwnershipVerificationRequest]

}
