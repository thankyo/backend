package com.clemble.loveit.thank.model

import com.clemble.loveit.common.model.{Resource, ResourceAware, Tag, UserID}
import com.clemble.loveit.common.util.WriteableUtils
import com.clemble.loveit.user.model.{User, UserAware}
import play.api.http.Writeable
import play.api.libs.json.{Json, OFormat}

case class SupportedProject(
                             resource: Resource,
                             user: UserID,
                             title: Option[String] = None,
                             description: Option[String] = None,
                             avatar: Option[String] = None,
                             tags: Set[Tag] = Set.empty
                           ) extends UserAware with ResourceAware

object SupportedProject {

  def error(res: Resource): SupportedProject = SupportedProject(res, User.UNKNOWN, Some("No owner registered for this resource"))

  implicit val jsonFormat: OFormat[SupportedProject] = Json.format[SupportedProject]

  implicit val projectWritable = WriteableUtils.jsonToWriteable[SupportedProject]()
  implicit val projectListWriteable: Writeable[List[SupportedProject]] = WriteableUtils.jsonToWriteable[List[SupportedProject]]

}

