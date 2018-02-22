package com.clemble.loveit.thank.model

import com.clemble.loveit.common.model._
import play.api.libs.json.{Json, OFormat}

/**
  * Simplified abstraction of OpenGraphObject
  * TODO We can extend it in future as needed, for now this is enough to get things from the ground
  */
case class OpenGraphObject(
                            url: String,
                            image: Option[OpenGraphImage] = None,
                            title: Option[String] = None,
                            description: Option[String] = None,
                            tags: Set[Tag] = Set.empty,
                          ) {

  def merge(ogObjOpt: Option[OpenGraphObject]): OpenGraphObject = {
    ogObjOpt match {
      case Some(ogObj) =>
        copy(
          image = image.orElse(ogObj.image),
          title = title.orElse(ogObj.title),
          description = title.orElse(ogObj.description),
          tags = tags ++ ogObj.tags
        )
      case None => this
    }
  }

  def normalize(): OpenGraphObject = {
    image match {
      case Some(img) if (img.url.startsWith("/")) =>
        val root = OpenGraphObject.getRootUrl(url)
        val normImg = img.copy(
          url = root + img.url
        )
        copy(image = Some(normImg))
      case _ =>
        this
    }
  }

}

/**
  * OpenGraph Image model
  */
case class OpenGraphImage(
                           url: String,
                           secureUrl: Option[String] = None,
                           imageType: Option[MimeType] = None,
                           width: Option[Int] = None,
                           height: Option[Int] = None,
                           alt: Option[String] = None,
                         )

object OpenGraphObject {

  implicit val imageFormat: OFormat[OpenGraphImage] = Json.format[OpenGraphImage];
  implicit val jsonFormat: OFormat[OpenGraphObject] = Json.format[OpenGraphObject]

  def getRootUrl(url: String) = {
    val separator = url.indexOf("/", 6)
    if (separator == -1) {
      url + "/"
    } else {
      url.substring(0, url.indexOf("/", 6))
    }
  }

}