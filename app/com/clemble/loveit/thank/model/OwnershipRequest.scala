package com.clemble.loveit.thank.model

import com.clemble.loveit.common.model.{Resource, UserID}
import play.api.libs.json._

/**
  * States of resource ownership verification process
  */
sealed trait OwnershipRequestStatus
case object Pending extends OwnershipRequestStatus
case object Running extends OwnershipRequestStatus
case object Verified extends OwnershipRequestStatus
case object NonVerified extends OwnershipRequestStatus

object OwnershipRequestStatus {

  implicit val jsonFormat = new Format[OwnershipRequestStatus] {
    val PENDING = JsString("pending")
    val RUNNING = JsString("running")
    val VERIFIED = JsString("verified")
    val NON_VERIFIED = JsString("notVerified")

    override def reads(json: JsValue): JsResult[OwnershipRequestStatus] = {
      json match {
        case PENDING => JsSuccess(Pending)
        case RUNNING => JsSuccess(Running)
        case VERIFIED => JsSuccess(Verified)
        case NON_VERIFIED => JsSuccess(NonVerified)
        case _ => JsError(s"Can't read ${json} as OwnershipRequestStatus")
      }
    }

    override def writes(o: OwnershipRequestStatus): JsValue = {
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
case class OwnershipRequest(
                             status: OwnershipRequestStatus,
                             resource: Resource,
                             ownershipType: OwnershipType,
                             requester: UserID
                           )

object OwnershipRequest {

  implicit val jsonFormat = Json.format[OwnershipRequest]

}
