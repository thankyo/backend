package com.clemble.loveit.common.model

import com.clemble.loveit.common.util.{IDGenerator, WriteableUtils}
import play.api.http.Writeable
import play.api.libs.json._

trait ProjectLike {
  val url: Resource
  val title: String
  val shortDescription: String
  val description: Option[String]
  val avatar: Option[String]
  val webStack: Option[WebStack]
  val tags: Set[Tag]
  val rss: Option[String]
}

case class ProjectPointer(
  url: Resource,
  user: UserID,
  title: String,
  avatar: Option[String],
  webStack: Option[WebStack],
  _id: ProjectID
) extends UserAware

object ProjectPointer {

  implicit val jsonFormat = Json.format[ProjectPointer]

  implicit def toPointer(project: Project) = project.toPointer()

}

trait ProjectAware {
  val project: ProjectPointer
}

case class Project(
  url: Resource,
  user: UserID,
  title: String,
  shortDescription: String,
  description: Option[String] = None,
  avatar: Option[String] = None,
  webStack: Option[WebStack] = None,
  tags: Set[Tag] = Set.empty,
  rss: Option[String] = None,
  _id: ProjectID = IDGenerator.generate()
) extends UserAware with ResourceAware with ProjectLike {

  def toPointer(): ProjectPointer = ProjectPointer(url, user, title, avatar, webStack, _id)

}

object Project {

  def error(url: Resource): Project = Project(url, User.UNKNOWN, "No owner registered for this resource", "Error on project location")

  implicit val jsonFormat: OFormat[Project] = Json.format[Project]

  implicit val projectWritable = WriteableUtils.jsonToWriteable[Project]()
  implicit val projectListWriteable: Writeable[Seq[Project]] = WriteableUtils.jsonToWriteable[Seq[Project]]

  def from(user: UserID, constructor: ProjectLike): Project = {
    Project(
      user = user,
      url = constructor.url,
      title = constructor.title,
      shortDescription = constructor.shortDescription,
      description = constructor.description,
      avatar = constructor.avatar,
      webStack = constructor.webStack,
      tags = constructor.tags,
      rss = constructor.rss
    )
  }
}

case class DibsProject(
  url: Resource,
  title: String,
  shortDescription: String,
  description: Option[String] = None,
  avatar: Option[String] = None,
  webStack: Option[WebStack] = None,
  tags: Set[Tag] = Set.empty,
  rss: Option[String] = None,
  whoisEmail: Option[Email] = None
) extends ProjectLike

object DibsProject {

  implicit val format = Json.format[DibsProject]

}

case class OwnedProject(
  url: Resource,
  title: String,
  shortDescription: String,
  description: Option[String] = None,
  avatar: Option[String] = None,
  webStack: Option[WebStack] = None,
  tags: Set[Tag] = Set.empty,
  rss: Option[String] = None
) extends ProjectLike {

  def asDibsProject(email: Option[Email]): DibsProject = {
    DibsProject(
      url = url,
      title = title,
      shortDescription = shortDescription,
      description = description,
      avatar = avatar,
      webStack = webStack,
      tags = tags,
      rss = rss,
      whoisEmail = email
    )
  }

}

object OwnedProject {

  implicit val jsonFormat = Json.format[OwnedProject]
  implicit val projectWriteable = WriteableUtils.jsonToWriteable[OwnedProject]

}

