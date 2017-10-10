package com.clemble.loveit.thank.model

import com.clemble.loveit.common.model.{Resource, UserID}
import com.clemble.loveit.common.util.WriteableUtils
import play.api.libs.json._

/**
  * States of resource ownership verification process
  */
sealed trait VerificationStatus
case object Pending extends VerificationStatus
case object Running extends VerificationStatus
case object Verified extends VerificationStatus
case object NotVerified extends VerificationStatus

object VerificationStatus {

  implicit val jsonFormat = new Format[VerificationStatus] {
    val PENDING = JsString("pending")
    val RUNNING = JsString("running")
    val VERIFIED = JsString("verified")
    val NON_VERIFIED = JsString("notVerified")

    override def reads(json: JsValue): JsResult[VerificationStatus] = {
      json match {
        case PENDING => JsSuccess(Pending)
        case RUNNING => JsSuccess(Running)
        case VERIFIED => JsSuccess(Verified)
        case NON_VERIFIED => JsSuccess(NotVerified)
        case _ => JsError(s"Can't read ${json} as OwnershipRequestStatus")
      }
    }

    override def writes(o: VerificationStatus): JsValue = {
      o match {
        case Pending => PENDING
        case Running => RUNNING
        case Verified => VERIFIED
        case NotVerified => NON_VERIFIED
      }
    }
  }

}

/**
  * Ownership request
  *
  * @param status   current status
  * @param resource resource in question
  */
case class ROVerification[T <: Resource](
                                                 status: VerificationStatus,
                                                 resource: T,
                                                 verificationCode: String
                                               )

object ROVerification {

  implicit val jsonFormat = Json.format[ROVerification[Resource]]
  implicit val httpWriteable = WriteableUtils.jsonToWriteable[ROVerification[Resource]]
  implicit val listHttpWriteable = WriteableUtils.jsonToWriteable[Set[ROVerification[Resource]]]

}
