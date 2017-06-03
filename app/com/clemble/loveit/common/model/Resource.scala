package com.clemble.loveit.common.model

import com.clemble.loveit.common.util.WriteableUtils
import play.api.data.validation.ValidationError
import play.api.http.Writeable
import play.api.libs.json._
import play.api.mvc.PathBindable

import scala.annotation.tailrec

sealed trait Resource {
  val uri: String
  def stringify(): String
  def parent(): Option[Resource]
  def parents(): List[Resource]
}

case class HttpResource(uri: String) extends Resource {
  override def stringify(): String = s"http/${uri}"
  override def parent(): Option[Resource] = {
    val parentIndex = uri.lastIndexOf("/")
    if (parentIndex > 0)
      Some(HttpResource(uri.substring(0, parentIndex)))
    else
      None
  }
  override def parents(): List[Resource] = {
    @tailrec
    def toParents(uri: List[String], agg: List[String]): List[String] = {
      if (uri.isEmpty) agg
      else toParents(uri.tail, (uri.reverse.mkString("/")) :: agg)
    }

    val normUri = uri.split("\\/").toList
    val parentUri = toParents(normUri.reverse, List.empty[String]).reverse

    parentUri.map(HttpResource)
  }
  def append(path: String): HttpResource = HttpResource(s"$uri/$path")
}

object Resource {

  private val HTTP_JSON = JsString("http")

  def toJsonTypeFlag(o: Resource): JsValue = {
    o match {
      case _: HttpResource => HTTP_JSON
    }
  }

  implicit val jsonFormat = new Format[Resource] {

    override def reads(json: JsValue): JsResult[Resource] = {
      val uriOpt = (json \ "uri").asOpt[String]
      (json \ "type", uriOpt) match {
        case (JsDefined(HTTP_JSON), Some(uri)) => JsSuccess(HttpResource(uri))
        case _ => JsError(__ \ "type", ValidationError(s"Invalid Resource value ${json}"))
      }
    }

    override def writes(o: Resource): JsValue = o match {
      case HttpResource(uri) => JsObject(
        Seq(
          "type" -> toJsonTypeFlag(o),
          "uri" -> JsString(uri)
        )
      )
    }
  }

  implicit val resourceWriteable: Writeable[Resource] = WriteableUtils.jsonToWriteable[Resource]
  implicit val setHttpWriteable = WriteableUtils.jsonToWriteable[Set[Resource]]

  def from(uriStr: String): Resource = {
    def toUriAndConstructor(uri: String): (String, Function[String, Resource]) = {
      uri match {
        case httpRes if (httpRes.startsWith("http/")) => uri.substring(5) -> HttpResource.apply
        case httpRes if (httpRes.startsWith("http:/")) => uri.substring(6) -> HttpResource.apply
        case httpsRes if (httpsRes.startsWith("https/")) => uri.substring(6) -> HttpResource.apply
        case httpsRes if (httpsRes.startsWith("https:/")) => uri.substring(7) -> HttpResource.apply
        case _ => uri -> HttpResource.apply
      }
    }

    def removeMultipleSlashes(uri: String): String = {
      uri.split("\\/").filterNot(_.isEmpty).mkString("/")
    }

    def removePrefix(uri: String): String = {
      if (uri.startsWith("/"))
        uri.substring(1)
      else
        uri
    }

    val (justUri, constructor) = toUriAndConstructor(uriStr)
    val normUri = removePrefix(removeMultipleSlashes(justUri))

    constructor(normUri)
  }

  implicit val stringToResource: PathBindable[Resource] = new PathBindable[Resource] {

    override def bind(key: String, value: String): Either[String, Resource] = {
      Right(from(value))
    }

    override def unbind(key: String, value: Resource): String = {
      value.stringify()
    }
  }
}