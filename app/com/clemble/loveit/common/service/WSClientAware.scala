package com.clemble.loveit.common.service

import com.clemble.loveit.common.model.Resource
import javax.inject.{Inject, Singleton}
import play.api.http.Status
import play.api.libs.ws.WSClient

import scala.concurrent.{ExecutionContext, Future}

trait WSClientAware {

  val client: WSClient

}

trait URLValidator {

  def isAlive(url: Resource): Future[Boolean]

  def findAlive(url: Resource): Future[Option[Resource]] = {
    val variations = if (url.startsWith("http")) {
      List(url)
    } else {
      List(s"https://${url}", s"https://www.${url}", s"http://${url}", s"http://www.${url}")
    }
    findAlive(variations)
  }

  def findAlive(urls: List[Resource]): Future[Option[Resource]]

}


@Singleton
case class SimpleURLValidator @Inject()(client: WSClient)(implicit val ec: ExecutionContext) extends URLValidator {

  def isAlive(url: Resource): Future[Boolean] = {
    client.url(url)
      .withFollowRedirects(false)
      .head()
      .map(res => res.status == Status.OK)
      .recover({ case _ => false })
  }

  def findAlive(urls: List[Resource]): Future[Option[Resource]] = {
    urls match {
      case Nil => Future.successful(None)
      case url :: xs =>
        isAlive(url).flatMap({
          case true => Future.successful(Some(url))
          case false => findAlive(xs)
        })
    }
  }

}

case object TestURLValidator extends URLValidator {

  override def isAlive(url: Resource): Future[Boolean] = Future.successful(true)

  override def findAlive(urls: List[Resource]): Future[Option[Resource]] = Future.successful(urls.headOption)

}