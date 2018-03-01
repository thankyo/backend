package com.clemble.loveit.common

import java.time.YearMonth

import play.api.libs.json._

import scala.annotation.tailrec

package object model {

  type Amount = Long
  type Email = String
  type UserID = String
  type ProjectID = String
  type PaymentID = String
  type Tag = String
  type MimeType = String
  type Resource = String

  implicit val yearMonthJsonFormat = new Format[YearMonth] {
    override def reads(json: JsValue): JsResult[YearMonth] = {
      json match {
        case JsString(yom) =>
          val year = yom.substring(0, 4).toInt
          val month = yom.substring(5).toInt
          JsSuccess(YearMonth.of(year, month))
        case _ =>
          JsError(s"Can't read ${json} as YearMonth")
      }
    }

    override def writes(o: YearMonth): JsValue = {
      JsString(s"${o.getYear}/${o.getMonthValue}")
    }
  }

  implicit class ResourceExtensions(url: Resource) {

    def parent(): Option[Resource] = {
      val parentIndex = url.lastIndexOf("/")
      if (parentIndex > 0)
        Some(url.substring(0, parentIndex))
      else
        None
    }

    def parents(): List[Resource] = {
      @tailrec
      def toParents(urlParts: List[String], agg: List[String]): List[String] = {
        if (urlParts.tail.tail.isEmpty) {
          agg
        } else {
          toParents(urlParts.tail, (urlParts.reverse.mkString("/")) :: agg)
        }
      }

      val normParts = url.split("\\/").toList
      toParents(normParts.reverse, List.empty[String]).reverse
    }

  }

}
