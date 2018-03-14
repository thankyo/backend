package com.clemble.loveit.thank.service

import java.time.LocalDateTime
import javax.inject.{Inject, Singleton}

import com.clemble.loveit.thank.model.{OpenGraphImage, OpenGraphObject}
import org.jsoup.Jsoup
import play.api.http.Status
import play.api.libs.json.{JsValue, Json}
import play.api.libs.ws.{WSClient, WSResponse}

import scala.concurrent.{ExecutionContext, Future}

trait PostEnrichService {

  def enrich(post: OpenGraphObject): Future[OpenGraphObject]

}

object PostEnrichService {

  def openGraphFromHTML(url: String, htmlStr: String): Option[OpenGraphObject] = {
    Option(Jsoup.parse(htmlStr)).map(doc => {
      val imageUrl = {
        val ogImage = doc.getElementsByAttributeValue("property", "og:image").first()
        val imageSrc = doc.getElementsByAttributeValue("property", "og:image:src").first()
        Option(ogImage).orElse(Option(imageSrc)).map(_.attr("content"))
      }

      val description = {
        val descriptionProp = doc.getElementsByAttributeValue("property", "og:description").first()
        Option(descriptionProp).map(_.attr("content"))
      }

      val title = {
        val titleProp = doc.getElementsByAttributeValue("property", "og:title").first()
        val title = Option(doc.getElementsByTag("title").first()).map(_.text())
        Option(titleProp).map(_.attr("property")).orElse(title)
      }

      OpenGraphObject(
        url,
        description = description,
        title = title,
        image = imageUrl.map(OpenGraphImage(_))
      )
    })
  }

  def openGraphFromFB(url: String, fb: JsValue): Option[OpenGraphObject] = {

    val imageUrl = (fb \ "og_object" \ "image" \ "src").asOpt[String]
    val description = (fb \ "og_object" \ "description").asOpt[String]
    val title = (fb \ "og_object" \ "title").asOpt[String]
    val pubDate = (fb \ "og_object" \ "update_date").asOpt[LocalDateTime]

    if (imageUrl.isDefined || description.isDefined || title.isDefined) {
      Some(
        OpenGraphObject(
          url,
          image = imageUrl.map(OpenGraphImage(_)),
          description = description,
          title = title,
          pubDate = pubDate
        )
      )
    } else {
      None
    }
  }

  def updateOG(origGO: OpenGraphObject, htmlStr: String, fb: JsValue = Json.obj()): OpenGraphObject = {
    val htmlOG = openGraphFromHTML(origGO.url, htmlStr)
    val fbOG = openGraphFromFB(origGO.url, fb)

    origGO.merge(fbOG).merge(htmlOG).normalize()
  }

}

@Singleton
case class SimplePostEnrichService @Inject()(
  wsClient: WSClient,
  implicit val ec: ExecutionContext
) extends PostEnrichService {

  private def isRedirect(res: WSResponse) = {
    res.status match {
      case Status.MOVED_PERMANENTLY => true
      case Status.TEMPORARY_REDIRECT => true
      case Status.PERMANENT_REDIRECT => true
      case _ => false
    }
  }

  private def doEnrich(ogObj: OpenGraphObject, allowRedirect: Boolean = true): Future[OpenGraphObject] = {
    wsClient
      .url(ogObj.url).withFollowRedirects(false)
      .get
      .flatMap(res => {
        res.header("Location").headOption match {
          case Some(url) if (isRedirect(res) && allowRedirect) =>
            val normUrl = if (url.indexOf("?") != -1) url.substring(0, url.indexOf("?")) else url
            doEnrich(ogObj.copy(url = normUrl), false)
          case None =>
            wsClient.url(s"https://graph.facebook.com/?id=${ogObj.url}")
              .get()
              .map(_.json)
              .map(fb => PostEnrichService.updateOG(ogObj, res.body, fb))
        }
      })
  }

  override def enrich(post: OpenGraphObject): Future[OpenGraphObject] = {
    doEnrich(post)
  }

}

case object TestPostEnrichService extends PostEnrichService {
  override def enrich(post: OpenGraphObject): Future[OpenGraphObject] = Future.successful(post)
}