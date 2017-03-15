package com.clemble.thank.model

import com.mohiva.play.silhouette.api.LoginInfo
import play.api.data.validation.ValidationError
import play.api.libs.json.{JsError, _}
import play.api.mvc.PathBindable

import scala.annotation.tailrec

sealed trait Resource {
  val uri: String
  def stringify(): String
  def parents(): List[Resource]
}

case class HttpResource(uri: String) extends Resource {
  override def stringify(): String = s"http/${uri}"
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
}
case class FacebookResource(uri: String) extends Resource {
  override def stringify(): String = s"facebook/${uri}"
  override def parents(): List[Resource] = List(this)
}


object Resource {

  implicit val jsonFormat = new Format[Resource] {
    val HTTP_JSON = JsString("http")
    val FACEBOOK_JSON = JsString("facebook")

    override def reads(json: JsValue): JsResult[Resource] = {
      val uriOpt = (json \ "uri").asOpt[String]
      (json \ "type", uriOpt) match {
        case (JsDefined(HTTP_JSON), Some(uri)) => JsSuccess(Resource from (s"http/${uri}"))
        case (JsDefined(FACEBOOK_JSON), Some(uri)) => JsSuccess(Resource from (s"facebook/${uri}"))
        case _ => JsError(__ \ "type", ValidationError(s"Invalid Resource value ${json}"))
      }
    }

    override def writes(o: Resource): JsValue = o match {
      case FacebookResource(uri) => JsObject(
        Seq(
          "type" -> FACEBOOK_JSON,
          "uri" -> JsString(uri)
        )
      )
      case HttpResource(uri) => JsObject(
        Seq(
          "type" -> HTTP_JSON,
          "uri" -> JsString(uri)
        )
      )
    }
  }

  def from(loginInfo: LoginInfo) = {
    loginInfo.providerID match {
      case "facebook" => FacebookResource(loginInfo.providerKey)
    }
  }

  def from(uriStr: String): Resource = {
    def toUriAndConstructor(uri: String): (String, Function[String, Resource]) = {
      uri match {
        case fbRes if (fbRes.startsWith("facebook/")) => uri.substring(9) -> FacebookResource.apply
        case httpRes if (httpRes.startsWith("http/")) => uri.substring(5) -> HttpResource.apply
        case httpsRes if (httpsRes.startsWith("https/")) => uri.substring(6) -> HttpResource.apply
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
