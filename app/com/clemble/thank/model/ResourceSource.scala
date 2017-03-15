package com.clemble.thank.model

import play.api.libs.json._

sealed trait ResourceSource
case object HTTPSource extends ResourceSource
case object FacebookSource extends ResourceSource

object ResourceSource {

  implicit val format = new Format[ResourceSource] {
    val httpJson = JsString("http")
    val facebookJson = JsString("facebook")

    override def reads(json: JsValue): JsResult[ResourceSource] = {
      json match {
        case httpJson => JsSuccess(HTTPSource)
        case facebook => JsSuccess(FacebookSource)
        case _ => JsError(s"Can't parse ${json} as URISource")
      }
    }

    override def writes(o: ResourceSource): JsValue = {
      o match {
        case HTTPSource => httpJson
        case FacebookSource => facebookJson
      }
    }
  }

}

