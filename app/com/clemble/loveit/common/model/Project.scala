package com.clemble.loveit.common.model

import com.clemble.loveit.common.util.{IDGenerator, WriteableUtils}
import play.api.http.Writeable
import play.api.libs.json._

sealed trait Verification
case object GoogleVerification extends Verification
case object TumblrVerification extends Verification
case object DibbsVerification extends Verification

object Verification {

  implicit val json: Format[Verification] = new Format[Verification] {

    val GOOGLE = JsString("google")
    val TUMBLR = JsString("tumblr")
    val DIBBS = JsString("dibbs")

    override def reads(json: JsValue): JsResult[Verification] = json match {
      case GOOGLE => JsSuccess(GoogleVerification)
      case TUMBLR => JsSuccess(TumblrVerification)
      case DIBBS => JsSuccess(DibbsVerification)
      case _ => JsError("Unknown verification type")
    }

    override def writes(o: Verification): JsValue = {
      o match {
        case GoogleVerification => GOOGLE
        case TumblrVerification => TUMBLR
        case DibbsVerification => DIBBS
      }
    }
  }

}

trait ProjectLike {
  val url: Resource
  val title: String
  val shortDescription: String
  val description: Option[String]
  val avatar: Option[String]
  val webStack: Option[WebStack]
  val tags: Set[Tag]
  val verification: Verification
  val rss: Option[String]
}

case class Project(
  url: Resource,
  user: UserID,
  title: String,
  shortDescription: String,
  verification: Verification,
  description: Option[String] = None,
  avatar: Option[String] = None,
  webStack: Option[WebStack] = None,
  tags: Set[Tag] = Set.empty,
  rss: Option[String] = None,
  _id: ProjectID = IDGenerator.generate()
) extends UserAware with ResourceAware with ProjectLike

object Project {

  def error(url: Resource): Project = Project(url, User.UNKNOWN, "No owner registered for this resource", "Error on project location", DibbsVerification)

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
      verification = constructor.verification,
      rss = constructor.rss
    )
  }
}

case class ProjectConstructor(
  url: Resource,
  title: String,
  shortDescription: String,
  verification: Verification,
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

