package com.clemble.loveit.thank.model

import com.clemble.loveit.common.model.{DibsProject, OwnedProject, Project, Resource, User, UserAware, UserID}
import com.clemble.loveit.common.util.WriteableUtils
import play.api.libs.json.Json

case class UserProjects(
  user: UserID,
  google: Seq[OwnedProject] = Seq.empty[OwnedProject],
  tumblr: Seq[OwnedProject] = Seq.empty[OwnedProject],
  email: Seq[OwnedProject] = Seq.empty[OwnedProject],
  dibs: Seq[DibsProject] = Seq.empty[DibsProject],
  installed: Seq[Project] = Seq.empty[Project]
) extends UserAware

object UserProjects {

  implicit val jsonFormat = Json.format[UserProjects]
  implicit val writeable = WriteableUtils.jsonToWriteable[UserProjects]

  def from(user: User): UserProjects = {
    UserProjects(user.id)
  }

}