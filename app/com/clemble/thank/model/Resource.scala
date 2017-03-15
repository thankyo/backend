package com.clemble.thank.model

import play.api.libs.json.Json
import play.api.mvc.PathBindable

case class Resource(
                     source: ResourceSource,
                     uri: String
                   )



object Resource {
  implicit val jsonFormat = Json.format[Resource]

  def fromUri(uri: String): Resource = {
    Resource(HTTPSource, uri)
  }

  implicit val stringToResource: PathBindable[Resource] = new PathBindable[Resource] {

    override def bind(key: String, value: String): Either[String, Resource] = {
      Right(fromUri(value))
    }

    override def unbind(key: String, value: Resource): String = {
      value.toString()
    }
  }
}
