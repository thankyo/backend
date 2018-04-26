package com.clemble.loveit.thank.service

import com.clemble.loveit.common.model._
import com.clemble.loveit.common.service.WSClientAware
import com.mohiva.play.silhouette.api.Logger
import javax.inject.{Inject, Named, Singleton}
import play.api.http.Status
import play.api.libs.json.JsValue
import play.api.libs.ws.WSClient

import scala.concurrent.{ExecutionContext, Future}

trait WHOISService {

  def fetchEmail(url: Resource): Future[Option[Email]]

}



@Singleton
case class SimpleWHOISService @Inject() (
  client: WSClient,
  @Named("thank.whois.key") apiKey: String,
  implicit val ec: ExecutionContext
) extends WSClientAware with WHOISService with Logger {

  private def toServiceCall(url: Resource): String = {
    s"https://www.whoisxmlapi.com/whoisserver/WhoisService?apiKey=${apiKey}&domainName=${url.toParentDomain()}&outputFormat=json"
  }

  override def fetchEmail(url: Resource): Future[Option[Email]] = {
    val serviceUrl = toServiceCall(url)
    logger.debug(s"Verifying with API - ${serviceUrl}")
    client.url(serviceUrl).get().map(res => {
      if (res.status == Status.OK) {
        SimpleWHOISService.whoisToEmail(res.json)
      } else {
        None
      }
    })
  }

}

object SimpleWHOISService {

  def whoisToEmail(json: JsValue): Option[Email] = {
    (json \ "WhoisRecord" \ "contactEmail").asOpt[String]
  }

}

case object TestWHOISService extends WHOISService {

  def fetchEmail(url: Resource): Future[Option[Email]] = Future.successful(Option.empty[Email])

}
