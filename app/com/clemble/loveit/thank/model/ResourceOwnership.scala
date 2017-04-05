package com.clemble.loveit.thank.model

import com.clemble.loveit.common.model.{Resource, ResourceAware}
import com.clemble.loveit.common.util.WriteableUtils
import play.api.libs.json.{JsValue, _}

sealed trait ResourceOwnership extends ResourceAware {
  val resource: Resource
  def owns(subject: Resource): Boolean
}

case class FullResourceOwnership(resource: Resource) extends ResourceOwnership {
  override def owns(subject: Resource): Boolean = {
    subject == resource || subject.parents().contains(resource)
  }
}

case class PartialResourceOwnership(resource: Resource) extends ResourceOwnership {
  override def owns(subject: Resource): Boolean = {
    subject == resource
  }
}

case class UnrealizedResourceOwnership(resource: Resource) extends ResourceOwnership {
  override def owns(subject: Resource): Boolean = {
    subject == resource
  }
}

object ResourceOwnership {

  def full(uri: Resource): ResourceOwnership = FullResourceOwnership(uri)

  def partial(uri: Resource): ResourceOwnership = PartialResourceOwnership(uri)

  def unrealized(uri: Resource): ResourceOwnership = UnrealizedResourceOwnership(uri)

  def toPossibleOwnerships(resource: Resource): List[ResourceOwnership] = {
    val fullAndUnrealized = resource.
      parents().
      flatMap(subResource => {
        List(ResourceOwnership.full(subResource), ResourceOwnership.unrealized(subResource))
      })
    ResourceOwnership.partial(resource) :: fullAndUnrealized
  }

  private val FULL = JsString("full")
  private val PARTIAL = JsString("partial")
  private val UNREALIZED = JsString("unrealized")

  def toJsonTypeFlag(o: ResourceOwnership): JsValue = {
    o match {
      case _: FullResourceOwnership => FULL
      case _: PartialResourceOwnership => PARTIAL
      case _: UnrealizedResourceOwnership => UNREALIZED
    }
  }

  implicit val jsonFormat = new Format[ResourceOwnership] {


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
      JsObject(
        Seq(
          "type" -> toJsonTypeFlag(o),
          "resource" -> Resource.jsonFormat.writes(o.resource)
        )
      )
    }

  }

  implicit val httpWriteable = WriteableUtils.jsonToWriteable[ResourceOwnership]

  implicit val listHttpWriteable = WriteableUtils.jsonToWriteable[Set[ResourceOwnership]]

}