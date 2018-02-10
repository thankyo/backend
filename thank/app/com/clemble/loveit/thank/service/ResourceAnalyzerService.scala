package com.clemble.loveit.thank.service

import javax.inject.Inject

import com.clemble.loveit.thank.model.{WebStack, WordPress}
import play.api.libs.json.JsObject
import play.api.libs.ws.WSClient

import scala.concurrent.{ExecutionContext, Future}

trait ResourceAnalyzerService {

  def analyze(url: String): Future[Option[WebStack]]

}

case class WappalyzerResourceAnalyzerService @Inject()(lookupUrl: String, wsClient: WSClient)(implicit ec: ExecutionContext) extends ResourceAnalyzerService {

  override def analyze(url: String): Future[Option[WebStack]] = {
    wsClient.url(lookupUrl)
      .addQueryStringParameters("url" -> url)
      .execute()
      .filter(_.status == 200)
      .map(resp => {
        val apps = (resp.json \ "applications").asOpt[List[JsObject]].getOrElse(List.empty[JsObject])
        val appNames = apps.map(_ \ "name").map(_.asOpt[WebStack]).flatten
        appNames.headOption
      })
  }

}
