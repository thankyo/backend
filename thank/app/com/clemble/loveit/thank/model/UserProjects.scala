package com.clemble.loveit.thank.model

import com.clemble.loveit.common.model.Resource
import com.clemble.loveit.common.util.WriteableUtils
import play.api.libs.json.Json

case class UserProjects(
  owned: Seq[Resource],
  installed: Seq[Project]
)

object UserProjects {

  implicit val jsonFormat = Json.format[UserProjects]
  implicit val writeable = WriteableUtils.jsonToWriteable[UserProjects]

}