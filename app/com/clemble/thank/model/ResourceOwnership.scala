package com.clemble.thank.model

import com.clemble.thank.util.URIUtils
import play.api.libs.json._

sealed trait ResourceOwnership {
  val uri: URI
  def owns(resource: ResourceOwnership): Boolean
  def normalize(): ResourceOwnership
}

case class FullResourceOwnership(uri: URI) extends ResourceOwnership {
  override def owns(resource: ResourceOwnership): Boolean = resource.uri.startsWith(uri)
  override def normalize(): FullResourceOwnership = FullResourceOwnership(URIUtils.normalize(uri))
}

case class PartialResourceOwnership(uri: URI) extends ResourceOwnership {
  override def owns(resource: ResourceOwnership): Boolean = resource.uri == uri
  override def normalize(): PartialResourceOwnership = PartialResourceOwnership(URIUtils.normalize(uri))
}

case class UnrealizedResourceOwnership(uri: URI) extends ResourceOwnership {
  override def owns(resource: ResourceOwnership): Boolean = resource.uri == uri
  override def normalize(): UnrealizedResourceOwnership = UnrealizedResourceOwnership(URIUtils.normalize(uri))
}

object ResourceOwnership {

  def full(uri: URI): ResourceOwnership = FullResourceOwnership(uri)

  def partial(uri: URI): ResourceOwnership = PartialResourceOwnership(uri)

  def unrealized(uri: URI): ResourceOwnership = UnrealizedResourceOwnership(uri)

  def toPossibleOwnerships(uriStr: URI): List[ResourceOwnership] = {
    val fullAndUnrealized = URIUtils.
      toParents(uriStr).
      flatMap(uri => {
        List(ResourceOwnership.full(uri), ResourceOwnership.unrealized(uri))
      })
    ResourceOwnership.partial(uriStr) :: fullAndUnrealized
  }

  implicit val jsonFormat = new Format[ResourceOwnership] {

    val FULL = JsString("full")
    val PARTIAL = JsString("partial")
    val UNREALIZED = JsString("unrealized")

    override def reads(json: JsValue): JsResult[ResourceOwnership] = {
      val uriOpt = (json \ "uri").asOpt[String]
      uriOpt.flatMap(uri => {
        (json \ "type") match {
          case JsDefined(FULL) => Some(FullResourceOwnership(uri))
          case JsDefined(PARTIAL) => Some(PartialResourceOwnership(uri))
          case JsDefined(UNREALIZED) => Some(UnrealizedResourceOwnership(uri))
          case _ => None
        }
      }).
        map(JsSuccess(_)).
        getOrElse({
          JsError(s"Can't read ${json}")
        })
    }

    override def writes(o: ResourceOwnership): JsValue = {
      val typeJson = o match {
        case _: FullResourceOwnership => FULL
        case _: PartialResourceOwnership => PARTIAL
        case _: UnrealizedResourceOwnership => UNREALIZED
      }
      JsObject(
        Seq(
          "type" -> typeJson,
          "uri" -> JsString(o.uri)
        )
      )
    }

  }

}