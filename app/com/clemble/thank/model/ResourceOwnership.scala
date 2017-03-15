package com.clemble.thank.model

import com.clemble.thank.util.URIUtils
import play.api.libs.json._

sealed trait ResourceOwnership {
  val resource: Resource
  def owns(subject: Resource): Boolean
  def normalize(): ResourceOwnership
}

case class FullResourceOwnership(resource: Resource) extends ResourceOwnership {
  override def owns(subject: Resource): Boolean = {
    val sameSource = subject.source == resource.source
    val subUri = resource.source match {
      case HTTPSource => subject.uri.startsWith(resource.uri)
      case _ => resource.uri == subject.uri
    }
    sameSource && subUri
  }
  override def normalize(): FullResourceOwnership = FullResourceOwnership(URIUtils.normalize(resource))
}

case class PartialResourceOwnership(resource: Resource) extends ResourceOwnership {
  override def owns(subject: Resource): Boolean = {
    val sameSource = subject.source == resource.source
    val sameUri = subject.uri == resource.uri
    sameSource && sameUri
  }
  override def normalize(): PartialResourceOwnership = {
    PartialResourceOwnership(URIUtils.normalize(resource))
  }
}

case class UnrealizedResourceOwnership(resource: Resource) extends ResourceOwnership {
  override def owns(subject: Resource): Boolean = {
    val sameSource = subject.source == resource.source
    val sameUri = subject.uri == resource.uri
    sameSource && sameUri
  }
  override def normalize(): UnrealizedResourceOwnership = UnrealizedResourceOwnership(URIUtils.normalize(resource))
}

object ResourceOwnership {

  def full(uri: Resource): ResourceOwnership = FullResourceOwnership(uri)

  def partial(uri: Resource): ResourceOwnership = PartialResourceOwnership(uri)

  def unrealized(uri: Resource): ResourceOwnership = UnrealizedResourceOwnership(uri)

  def toPossibleOwnerships(resource: Resource): List[ResourceOwnership] = {
    val fullAndUnrealized = URIUtils.
      toParents(resource).
      flatMap(subResource => {
        List(ResourceOwnership.full(subResource), ResourceOwnership.unrealized(subResource))
      })
    ResourceOwnership.partial(resource) :: fullAndUnrealized
  }

  implicit val jsonFormat = new Format[ResourceOwnership] {

    val FULL = JsString("full")
    val PARTIAL = JsString("partial")
    val UNREALIZED = JsString("unrealized")

    override def reads(json: JsValue): JsResult[ResourceOwnership] = {
      val resourceOpt = (json \ "resource").asOpt[Resource]
      resourceOpt.flatMap(uri => {
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
          "resource" -> Resource.jsonFormat.writes(o.resource)
        )
      )
    }

  }

}