package com.clemble.loveit.thank.model

import com.clemble.loveit.common.model._
import play.api.libs.json.{Json, OFormat}

/**
  * Simplified abstraction of OpenGraphObject
  * TODO We can extend it in future as needed, for now this is enough to get things from the ground
  */
case class OpenGraphObject(
  url: String,
  title: Option[String] = None,
  image: Option[OpenGraphImage] = None,
  description: Option[String] = None,
)

/**
  * OpenGraph Image model
  */
case class OpenGraphImage(
  url: String,
  secureUrl: Option[String] = None,
  imageType: Option[MimeType] = None,
  width: Option[Int] = None,
  height: Option[Int] = None,
  alt: Option[String] = None
)

object OpenGraphObject {

  implicit val imageFormat: OFormat[OpenGraphImage] = Json.format[OpenGraphImage];
  implicit val jsonFormat: OFormat[OpenGraphObject] = Json.format[OpenGraphObject]

}