package com.clemble.loveit.thank.model

import com.clemble.loveit.common.model.Resource
import com.clemble.loveit.common.util.WriteableUtils
import play.api.libs.json.Json

case class OwnedProjects(
  pending: Seq[Resource],
  installed: Seq[Project]
)

object OwnedProjects {

  implicit val jsonFormat = Json.format[OwnedProjects]
  implicit val writeable = WriteableUtils.jsonToWriteable[OwnedProjects]

}