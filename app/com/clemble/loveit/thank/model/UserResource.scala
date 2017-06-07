package com.clemble.loveit.thank.model

import com.clemble.loveit.common.model.{Resource, UserID}
import com.clemble.loveit.common.util.WriteableUtils
import play.api.libs.json.{Format, JsResult, JsValue, Json}

/**
  * Model required for UserResource
  */
trait UserResource {
  /**
    * User identifier
    */
  val id: UserID
  /**
    * Set of owned resources
    */
  val owns: Set[Resource]
  /**
    * Current pending verification
    */
  val verification: Option[ROVerification[Resource]]
}

object UserResource {

  def empty(user: UserID): UserResource = SimpleUserResource(user)

  implicit val jsonFormat: Format[UserResource] = new Format[UserResource] {
    override def reads(json: JsValue): JsResult[UserResource] = SimpleUserResource.jsonFormat.reads(json)
    override def writes(o: UserResource): JsValue = SimpleUserResource.jsonFormat.writes(SimpleUserResource(o.id, o.owns, o.verification))
  }

  implicit val writeable = WriteableUtils.jsonToWriteable[UserResource]()
}

private case class SimpleUserResource(
                               id: UserID,
                               owns: Set[Resource] = Set.empty,
                               verification: Option[ROVerification[Resource]] = None
                             ) extends UserResource

private object SimpleUserResource {
  implicit val jsonFormat = Json.format[SimpleUserResource]
}


