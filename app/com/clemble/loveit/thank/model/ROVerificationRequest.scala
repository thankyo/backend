package com.clemble.loveit.thank.model

import com.clemble.loveit.common.model.{Resource, UserID}
import com.clemble.loveit.common.util.WriteableUtils
import play.api.libs.json._

/**
  * States of resource ownership verification process
  */
sealed trait ROVerificationRequestStatus

case object Pending extends ROVerificationRequestStatus

case object Running extends ROVerificationRequestStatus

case object Verified extends ROVerificationRequestStatus

case object NonVerified extends ROVerificationRequestStatus

object ROVerificationRequestStatus {

  implicit val jsonFormat = new Format[ROVerificationRequestStatus] {
    val PENDING = JsString("pending")
    val RUNNING = JsString("running")
    val VERIFIED = JsString("verified")
    val NON_VERIFIED = JsString("notVerified")

    override def reads(json: JsValue): JsResult[ROVerificationRequestStatus] = {
      json match {
        case PENDING => JsSuccess(Pending)
        case RUNNING => JsSuccess(Running)
        case VERIFIED => JsSuccess(Verified)
        case NON_VERIFIED => JsSuccess(NonVerified)
        case _ => JsError(s"Can't read ${json} as OwnershipRequestStatus")
      }
    }

    override def writes(o: ROVerificationRequestStatus): JsValue = {
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
  * @param status   current status
  * @param resource resource in question
  */
case class ROVerificationRequest[T <: Resource](
                                                 id: VerificationID,
                                                 status: ROVerificationRequestStatus,
                                                 resource: T,
                                                 requester: UserID,
                                                 verificationCode: String
                                               )

object ROVerificationRequest {

  implicit val jsonFormat = Json.format[ROVerificationRequest[Resource]]
  implicit val httpWriteable = WriteableUtils.jsonToWriteable[ROVerificationRequest[Resource]]
  implicit val listHttpWriteable = WriteableUtils.jsonToWriteable[Set[ROVerificationRequest[Resource]]]

}
