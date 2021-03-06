package com.clemble.loveit.common.model

import java.time.LocalDateTime

import play.api.libs.json.{Json, OFormat}

/**
  * Simplified abstraction of OpenGraphObject
  * TODO We can extend it in future as needed, for now this is enough to get things from the ground
  */
case class OpenGraphObject(
  url: Resource,
  image: Option[OpenGraphImage] = None,
  title: Option[String] = None,
  description: Option[String] = None,
  tags: Set[Tag] = Set.empty,
  pubDate: Option[LocalDateTime] = None
) extends ResourceAware {

  def merge(ogObj: OpenGraphObject): OpenGraphObject = {
    copy(
      image = image.orElse(ogObj.image),
      title = title.orElse(ogObj.title),
      description = title.orElse(ogObj.description),
      tags = tags ++ ogObj.tags,
      pubDate = pubDate.orElse(ogObj.pubDate)
    )
  }

  def merge(ogObjOpt: Option[OpenGraphObject]): OpenGraphObject = {
    ogObjOpt match {
      case Some(ogObj) => merge(ogObj)
      case None => this
    }
  }

  def normalize(): OpenGraphObject = {
    image match {
      case Some(img) if img.url.startsWith("/") =>
        val root = OpenGraphObject.getRootUrl(url)
        val normImg = img.copy(
          url = root + img.url
        )
        copy(image = Some(normImg))
      case _ =>
        this
    }
  }

  def withTags(additionalTags: Set[Tag]): OpenGraphObject = {
    this.copy(tags = tags ++ additionalTags)
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
    val separator = url.indexOf("/", 8)
    if (separator == -1) {
      url + "/"
    } else {
      url.substring(0, separator + 1)
    }
  }

}