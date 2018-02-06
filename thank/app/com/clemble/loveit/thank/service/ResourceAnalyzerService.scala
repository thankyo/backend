package com.clemble.loveit.thank.service

import javax.inject.Inject

import play.api.libs.json.JsObject
import play.api.libs.ws.WSClient

import scala.concurrent.{ExecutionContext, Future}

trait ResourceAnalyzerService {

  def analyze(url: String): Future[Option[String]]

}

case class WappalyzerResourceAnalyzerService @Inject()(lookupUrl: String, wsClient: WSClient)(implicit ec: ExecutionContext) extends ResourceAnalyzerService {

  override def analyze(url: String): Future[Option[String]] = {
    wsClient.url(lookupUrl)
      .addQueryStringParameters("url" -> url)
      .execute()
      .filter(_.status == 200)
      .map(resp => {
        val apps = (resp.json \ "applications").asOpt[List[JsObject]].getOrElse(List.empty[JsObject])
        val appNames = apps.map(_ \ "name").map(_.asOpt[String]).flatten
        appNames.find(_ == "WordPress")
      })
  }

}
