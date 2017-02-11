package com.clemble.thank.util

import java.net.URI

import scala.annotation.tailrec

object URIUtils {

  def split(uriStr: String): List[String] = {
    val uri = new URI(uriStr)
    val host = uri.getHost()
    val path = uri.getPath().split("\\/").toList.filterNot(_.isEmpty())
    (uri.getScheme :: host :: path).filterNot(_ == null)
  }

  def toParents(uriStr: String): List[String] = {
    @tailrec
    def toParents(uri: List[String], agg: List[String]): List[String] = {
      if (uri.isEmpty || uri.tail.isEmpty) agg
      else toParents(uri.tail, normalize(uri.reverse) :: agg)
    }
    toParents(split(uriStr).reverse, List.empty[String]).reverse
  }

  def normalize(uri: List[String]): String = {
    uri.mkString("/")
  }

}
