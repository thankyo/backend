package com.clemble.loveit.thank.model

import com.clemble.loveit.common.model.{Resource, UserID}
import com.clemble.loveit.common.util.WriteableUtils
import com.clemble.loveit.user.model.{User, UserAware}
import play.api.libs.json._

/**
  * Model required for UserResource
  */
case class UserResource(
                         _id: UserID,
                         owns: Set[SupportedProject] = Set.empty[SupportedProject],
                         verification: Option[ROVerification[Resource]] = None
) extends UserAware {

  val user = _id

}

object UserResource {

  def from(user: User): UserResource = UserResource(user.id)

  implicit val jsonFormat: OFormat[UserResource] = Json.format[UserResource]
  implicit val writeable = WriteableUtils.jsonToWriteable[UserResource]()

}

