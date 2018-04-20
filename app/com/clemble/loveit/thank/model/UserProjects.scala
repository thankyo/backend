package com.clemble.loveit.thank.model

import com.clemble.loveit.common.model.{OwnedProject, Project, Resource, UserAware, UserID}
import com.clemble.loveit.common.util.WriteableUtils
import play.api.libs.json.Json

case class UserProjects(
  user: UserID,
  owned: Seq[OwnedProject],
  installed: Seq[Project]
) extends UserAware

object UserProjects {

  implicit val jsonFormat = Json.format[UserProjects]
  implicit val writeable = WriteableUtils.jsonToWriteable[UserProjects]

}