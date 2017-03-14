package com.clemble.thank.model

import com.clemble.thank.util.URIUtils
import play.api.libs.json._

sealed trait ResourceOwnership {
  val uri: String
  def owns(resource: ResourceOwnership): Boolean
}

case class FullResourceOwnership(uri: String) extends ResourceOwnership {
  override def owns(resource: ResourceOwnership): Boolean = resource.uri.startsWith(uri)
}

case class PartialResourceOwnership(uri: String) extends ResourceOwnership {
  override def owns(resource: ResourceOwnership): Boolean = resource.uri == uri
}

case class UnrealizedResourceOwnership(uri: String) extends ResourceOwnership {
  override def owns(resource: ResourceOwnership): Boolean = resource.uri == uri
}

object ResourceOwnership {

  def full(uri: String): ResourceOwnership = FullResourceOwnership(uri)

  def partial(uri: String): ResourceOwnership = PartialResourceOwnership(uri)

  def unrealized(uri: String): ResourceOwnership = UnrealizedResourceOwnership(uri)

  def toPossibleOwnerships(uriStr: String): List[ResourceOwnership] = {
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