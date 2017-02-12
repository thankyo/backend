package com.clemble.thank.model

import play.api.libs.json._

sealed trait ResourceOwnership {
  val uri: String
}
case class FullResourceOwnership(uri: String) extends ResourceOwnership
case class PartialResourceOwnership(uri: String) extends ResourceOwnership
case class UnrealizedResourceOwnership(uri: String) extends ResourceOwnership

object ResourceOwnership {

  def full(uri: String) = FullResourceOwnership(uri)
  def partial(uri: String) = PartialResourceOwnership(uri)
  def unrealized(uri: String) = UnrealizedResourceOwnership(uri)

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
        getOrElse({ JsError(s"Can't read ${json}") })
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