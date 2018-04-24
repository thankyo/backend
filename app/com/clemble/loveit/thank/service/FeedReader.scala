package com.clemble.loveit.thank.service

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

import com.clemble.loveit.common.model.{OpenGraphObject}
import com.clemble.loveit.common.model.OpenGraphImage

import scala.util.Try
import scala.xml.{Elem, Node}

trait FeedParser {

  def from(tagName: String)(implicit source: Node): Option[String] = {
    val x = source \ tagName
    if (x.isEmpty) {
      None
    } else {
      Some(x.text)
    }
  }

  def parseDate(x: String)(implicit format: DateTimeFormatter): Option[LocalDateTime] = {
    Try(LocalDateTime.parse(x.toCharArray, format)).toOption
  }

  def parse(x: Elem): Iterable[Option[OpenGraphObject]]

}

case object FeedParser {
  private def matchParser(x: Elem) = {
    if (x.label == "RDF" || x.label == "rss") {
      RSSParser
    } else {
      AtomParser
    }
  }

  def parse(x: Elem): Iterable[OpenGraphObject] = {
    matchParser(x).parse(x).flatten
  }
}

case object RSSParser extends FeedParser {
  val _title = "title"
  val _origLink = "origLink"
  val _link = "link"
  val _description = "description"
  val _item = "item"
  val _pubDate = "pubDate"
  val _author = "author"

  implicit val format = DateTimeFormatter.RFC_1123_DATE_TIME

  override def parse(x: Elem): Iterable[Option[OpenGraphObject]] = {
    val items = x \\ _item
    items.map({ implicit item =>
      val pubDateStrOpt = from(_pubDate)
      val pubDate = pubDateStrOpt.flatMap(parseDate)
      val opOpt = from(_origLink).orElse(from(_link)).map(url => {
        OpenGraphObject(
          url = url,
          title = from(_title),
          description = from(_description),
          image = Option(item \ "thumbnail").filter(_.nonEmpty).flatMap(img => {
            val height = Option(img.\@("height")).filterNot(_.isEmpty).map(_.toInt)
            val width = Option(img.\@("width")).filterNot(_.isEmpty).map(_.toInt)
            Option(img.\@("url")).map(url => OpenGraphImage(url, height = height, width = width))
          }),
          pubDate = pubDate
        )
      })
      opOpt
    })
  }

}

case object AtomParser extends FeedParser {
  val _author = "author"
  val _name = "name"
  val _link = "link"
  val _updated = "updated"
  val _title = "title"
  val _entry = "entry"
  val _summary = "summary"

  protected def getAuthors(x: Node) = {
    val r = x \ _author
    if (r.isEmpty) {
      None
    } else {
      Some(r.flatMap(e => (e \ _name).map(_.text)).mkString(", "))
    }
  }

  protected def getLinks(x: Node) = {
    val links = x \ _link
    val r = links
      .filter(_.attribute("rel").exists(_.forall(_.text == "alternate")))
      .flatMap(_.attribute("href").map(_.text)).headOption

    if (links.nonEmpty && r.isEmpty) {
      links.headOption.flatMap(_.attribute("href").map(_.text))
    } else {
      r
    }
  }

  implicit val format = DateTimeFormatter.ISO_DATE_TIME

  override def parse(x: Elem): Iterable[Option[OpenGraphObject]] = {
    // global author
    val xs = x \ _author
    val g = if (xs.nonEmpty) {
      (xs \ _name).headOption.map(_.text).orElse(Some(xs.text))
    } else {
      None
    }

    val items = (x \ _entry)
    items.map({ implicit entry =>
      getLinks(entry)
        .map(url => {
          OpenGraphObject(
            url = url,
            title = from(_title),
            description = from(_summary),
            pubDate = from(_updated).flatMap(parseDate)
          )
        })
    })
  }

}