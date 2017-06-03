package com.clemble.loveit.thank.service

import javax.inject.{Inject, Singleton}

import com.clemble.loveit.common.model.HttpResource
import play.api.libs.ws.WSClient

import scala.concurrent.{ExecutionContext, Future}

sealed trait MetaTagReader {
  def read(res: HttpResource): Future[Option[String]]
}

object MetaTagReader {

  private val META_DESCRIPTION = """.*meta\s+name="loveit-site-verification"\s*content="([^"]+)"""".r

  def findInHtml(html: String): Option[String] = {
    META_DESCRIPTION.findFirstMatchIn(html).map(_.group(1))
  }

}


@Singleton
case class WSMetaTagReader @Inject()(wsClient: WSClient, implicit val ec: ExecutionContext) extends MetaTagReader {

  def read(res: HttpResource): Future[Option[String]] = {
    for {
      html <- wsClient.url(s"http://${res.uri}").execute()
      secHtml <- wsClient.url(s"https://${res.uri}").execute()
    } yield {
      MetaTagReader.findInHtml(html.body).orElse(MetaTagReader.findInHtml(secHtml.body))
    }
  }

}