package com.clemble.loveit.thank.model

import com.clemble.loveit.common.model.{Resource, ResourceAware}
import com.clemble.loveit.common.util.WriteableUtils
import play.api.libs.json.Json

case class ResourceOwnership(resource: Resource, ownershipType: OwnershipType) extends ResourceAware {
  def owns(subject: Resource): Boolean = ownershipType.owns(resource, subject)
}

object ResourceOwnership {

  def full(uri: Resource): ResourceOwnership = ResourceOwnership(uri, FullOwnership)

  def partial(uri: Resource): ResourceOwnership = ResourceOwnership(uri, PartialOwnership)

  def unrealized(uri: Resource): ResourceOwnership = ResourceOwnership(uri, UnrealizedOwnership)

  def toPossibleOwnerships(resource: Resource): List[ResourceOwnership] = {
    val fullAndUnrealized = resource.
      parents().
      flatMap(subResource => {
        List(ResourceOwnership.full(subResource), ResourceOwnership.unrealized(subResource))
      })
    ResourceOwnership.partial(resource) :: fullAndUnrealized
  }

  implicit val jsonFormat = Json.format[ResourceOwnership]

  implicit val httpWriteable = WriteableUtils.jsonToWriteable[ResourceOwnership]
  implicit val listHttpWriteable = WriteableUtils.jsonToWriteable[Set[ResourceOwnership]]

}