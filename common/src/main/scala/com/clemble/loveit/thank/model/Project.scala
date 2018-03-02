package com.clemble.loveit.thank.model

import com.clemble.loveit.common.model._
import com.clemble.loveit.common.util.{IDGenerator, WriteableUtils}
import com.clemble.loveit.user.model.{User, UserAware}
import play.api.http.Writeable
import play.api.libs.json.{Json, OFormat}

case class Project(
                             url: Resource,
                             user: UserID,
                             title: Option[String] = None,
                             description: Option[String] = None,
                             avatar: Option[String] = None,
                             webStack: Option[WebStack] = None,
                             tags: Set[Tag] = Set.empty,
                             rss: Option[String] = None,
                             _id: ProjectID = IDGenerator.generate()
                           ) extends UserAware with ResourceAware

object Project {

  def error(url: Resource): Project = Project(url, User.UNKNOWN, Some("No owner registered for this resource"))

  implicit val jsonFormat: OFormat[Project] = Json.format[Project]

  implicit val projectWritable = WriteableUtils.jsonToWriteable[Project]()
  implicit val projectListWriteable: Writeable[Seq[Project]] = WriteableUtils.jsonToWriteable[Seq[Project]]

}

