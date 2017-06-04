package com.clemble.loveit.thank.model

import com.clemble.loveit.common.model.{Resource, UserID}
import com.clemble.loveit.common.util.WriteableUtils
import play.api.libs.json.{Format, JsResult, JsValue, Json}

/**
  * Model required for UserResource
  */
trait UserResource {
  val id: UserID
  val owns: Set[Resource]
  val pending: Option[ROVerification[Resource]]
}

object UserResource {

  def empty(user: UserID) = SimpleUserResource(user)

  implicit val jsonFormat: Format[UserResource] = new Format[UserResource] {
    override def reads(json: JsValue): JsResult[UserResource] = SimpleUserResource.jsonFormat.reads(json)
    override def writes(o: UserResource): JsValue = SimpleUserResource.jsonFormat.writes(SimpleUserResource(o.id, o.owns, o.pending))
  }

  implicit val writeable = WriteableUtils.jsonToWriteable[UserResource]()
}

case class SimpleUserResource(
                               id: UserID,
                               owns: Set[Resource] = Set.empty,
                               pending: Option[ROVerification[Resource]] = None
                             ) extends UserResource

object SimpleUserResource {
  implicit val jsonFormat = Json.format[SimpleUserResource]
}


