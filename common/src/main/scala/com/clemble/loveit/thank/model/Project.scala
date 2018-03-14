package com.clemble.loveit.thank.model

import com.clemble.loveit.common.model._
import com.clemble.loveit.common.util.{IDGenerator, WriteableUtils}
import com.clemble.loveit.user.model.{User, UserAware}
import play.api.http.Writeable
import play.api.libs.json.{Json, OFormat}

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
) extends UserAware with ResourceAware with ProjectLike

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

case class ProjectConstructor(
  url: Resource,
  title: String,
  shortDescription: String,
  description: Option[String] = None,
  avatar: Option[String] = None,
  webStack: Option[WebStack] = None,
  tags: Set[Tag] = Set.empty,
  rss: Option[String] = None
) extends ProjectLike

object ProjectConstructor {

  implicit val jsonFormat = Json.format[ProjectConstructor]
  implicit val projectWriteable = WriteableUtils.jsonToWriteable[ProjectConstructor]

}

