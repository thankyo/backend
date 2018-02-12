package com.clemble.loveit.thank.service

import javax.inject.Inject

import com.clemble.loveit.thank.model.{Project, WebStack}
import com.clemble.loveit.user.service.UserService
import play.api.libs.json.JsObject
import play.api.libs.ws.WSClient

import scala.concurrent.{ExecutionContext, Future}

trait ProjectEnrichService {

  def enrich(project: Project): Future[Project]

}

case class SimpleProjectEnrichService @Inject()(lookupUrl: String, wsClient: WSClient, userService: UserService)(implicit ec: ExecutionContext) extends ProjectEnrichService {

  private def analyzeWebStack(url: String): Future[Option[WebStack]] = {
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

  private def enrichWebStack(project: Project): Future[Option[WebStack]] = {
    val uri = project.resource.uri
    for {
      webStacks <- Future.sequence(List(analyzeWebStack(s"http://${uri}"), analyzeWebStack(s"https://${uri}")))
    } yield {
      webStacks.flatten.headOption.orElse(project.webStack)
    }
  }

  private def enrichAvatar(project: Project): Future[Option[String]] = {
    if (project.avatar.isDefined) {
      return Future.successful(project.avatar)
    }
    userService.findById(project.user).map(_.flatMap(_.avatar))
  }

  override def enrich(project: Project): Future[Project] = {
    for {
      webStack <- enrichWebStack(project)
      avatar <- enrichAvatar(project)
    } yield {
      project.copy(webStack = webStack, avatar = avatar, title = project.title.orElse(Some(project.resource.uri)))
    }
  }

}