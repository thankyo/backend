package com.clemble.loveit.thank.model

import com.clemble.loveit.common.model.UserID
import com.clemble.loveit.common.util.WriteableUtils
import com.clemble.loveit.user.model.{User, UserAware}
import play.api.libs.json.{Json, OFormat}

case class SupportedProject(
                             id: UserID,
                             firstName: Option[String] = None,
                             lastName: Option[String] = None,
                             avatar: Option[String] = None,
                             bio: Option[String] = None,
                             link: Option[String] = None,
                             tags: List[String] = List.empty
                           ) extends UserAware {

  val user: UserID = id

}

object SupportedProject {

  val empty = SupportedProject("unknown")

  def from(user: User): SupportedProject = {
    SupportedProject(user.id, user.firstName, user.lastName, user.avatar, user.bio, user.link)
  }

  implicit val jsonFormat: OFormat[SupportedProject] = Json.format[SupportedProject]

  implicit val writableFormat = WriteableUtils.jsonToWriteable[SupportedProject]()

}

