package com.clemble.loveit.thank.service

import javax.inject.Inject

import com.clemble.loveit.thank.model.{Project, WebStack}
import play.api.libs.json.JsObject
import play.api.libs.ws.WSClient

import scala.concurrent.{ExecutionContext, Future}

trait ProjectEnrichService {

  def enrich(project: Project): Future[Project]

}

case class SimpleProjectEnrichService @Inject()(lookupUrl: String, wsClient: WSClient)(implicit ec: ExecutionContext) extends ProjectEnrichService {

  def analyzeWebStack(url: String): Future[Option[WebStack]] = {
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

  override def enrich(project: Project): Future[Project] = {
    if (project.webStack.isDefined) {
      return Future.successful(project)
    }

    val uri = project.resource.uri
    for {
      webStacks <- Future.sequence(List(analyzeWebStack(s"http://${uri}"), analyzeWebStack(s"https://${uri}")))
    } yield {
      val possibleStack = webStacks.flatten.headOption.orElse(project.webStack)
      project.copy(webStack = possibleStack)
    }
  }

}