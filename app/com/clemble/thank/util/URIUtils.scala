package com.clemble.thank.util

import scala.annotation.tailrec

object URIUtils {

  def toParents(uriStr: String): List[String] = {
    def toURI(uri: List[String]): String = {
      uri.mkString("/")
    }

    @tailrec
    def toParents(uri: List[String], agg: List[String]): List[String] = {
      if (uri.isEmpty) agg
      else toParents(uri.tail, toURI(uri.reverse) :: agg)
    }

    val normUri = normalize(uriStr).split("\\/").toList
    toParents(normUri.reverse, List.empty[String]).reverse
  }

  def normalize(uri: String): String = {
    def removeHttpPrefix(uri: String): String = {
      if (uri.startsWith("http/")) {
        uri.substring(5)
      } else if (uri.startsWith("https/")) {
        uri.substring(6)
      } else {
        uri
      }
    }

    def removeMultipleSlashes(uri: String): String = {
      uri.split("\\/").filterNot(_.isEmpty).mkString("/")
    }

    def removePrefix(uri: String): String = {
      if (uri.startsWith("/"))
        uri.substring(1)
      else
        uri
    }

    removePrefix(
      removeHttpPrefix(removeMultipleSlashes(uri))
    )
  }

}
