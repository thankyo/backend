package com.clemble.loveit.thank.model

import com.clemble.loveit.common.model.{DibsProject, EmailProject, OwnedProject, Project, ProjectLike, Resource, User, UserAware, UserID}
import com.clemble.loveit.common.util.WriteableUtils
import play.api.libs.json.Json

case class UserProject(
  user: UserID,
  google: Seq[OwnedProject] = Seq.empty[OwnedProject],
  tumblr: Seq[OwnedProject] = Seq.empty[OwnedProject],
  email: Seq[EmailProject] = Seq.empty[EmailProject],
  dibs: Seq[DibsProject] = Seq.empty[DibsProject],
  installed: Seq[Project] = Seq.empty[Project]
) extends UserAware {

}

object UserProject {

  implicit val jsonFormat = Json.format[UserProject]
  implicit val writeable = WriteableUtils.jsonToWriteable[UserProject]

  def from(user: User): UserProject = {
    UserProject(user.id)
  }

}