package com.clemble.loveit.thank.model

import com.clemble.loveit.common.model.Resource
import play.api.libs.json._

/**
  * Ownership can be of different nature
  */
sealed trait OwnershipType {
  def owns(resource: Resource, subject: Resource): Boolean
}

case object FullOwnership extends OwnershipType {
  override def owns(resource: Resource, subject: Resource): Boolean = {
    subject == resource || subject.parents().contains(resource)
  }
}
case object PartialOwnership extends OwnershipType {
  override def owns(resource: Resource, subject: Resource): Boolean = {
    subject == resource
  }
}
case object UnrealizedOwnership extends OwnershipType {
  override def owns(resource: Resource, subject: Resource): Boolean = {
    subject == resource
  }
}

object OwnershipType {

  implicit val jsonFormat = new Format[OwnershipType] {

    private val FULL = JsString("full")
    private val PARTIAL = JsString("partial")
    private val UNREALIZED = JsString("unrealized")

    override def reads(json: JsValue): JsResult[OwnershipType] = json match {
      case FULL => JsSuccess(FullOwnership)
      case PARTIAL => JsSuccess(PartialOwnership)
      case UNREALIZED => JsSuccess(UnrealizedOwnership)
      case _ => JsError(s"Can't deserialze ${json} as OwnershipType")
    }

    override def writes(o: OwnershipType): JsValue = o match {
      case FullOwnership => FULL
      case PartialOwnership => PARTIAL
      case UnrealizedOwnership => UNREALIZED
    }
  }

}