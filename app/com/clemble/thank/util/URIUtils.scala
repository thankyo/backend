package com.clemble.thank.util

import com.clemble.thank.model.{FacebookSource, HTTPSource, Resource, ResourceSource}
import com.mohiva.play.silhouette.api.LoginInfo

import scala.annotation.tailrec

object URIUtils {

  def toUri(profile: LoginInfo): Resource = {
    profile.providerID match {
      case "facebook" => Resource(FacebookSource, profile.providerKey)
      case "test" => Resource(HTTPSource, profile.providerKey)
    }
  }

  private def toHttpParents(uri: String): List[String] = {
    def toURI(uri: List[String]): String = {
      uri.mkString("/")
    }

    @tailrec
    def toParents(uri: List[String], agg: List[String]): List[String] = {
      if (uri.isEmpty) agg
      else toParents(uri.tail, toURI(uri.reverse) :: agg)
    }

    val normUri = normalize(uri).uri.split("\\/").toList
    toParents(normUri.reverse, List.empty[String]).reverse
  }

  def toParents(uri: Resource): List[Resource] = {
    uri.source match {
      case FacebookSource => List(uri)
      case HTTPSource => toHttpParents(uri.uri).map(Resource(HTTPSource, _))
    }
  }

  def normalize(resource: Resource): Resource = {
    resource
  }

  def normalize(uriStr: String): Resource = {
    def toSource(uri: String): ResourceSource = uri match {
      case facebookResource if (facebookResource.startsWith("facebook/")) => FacebookSource
      case httpResource if (httpResource.startsWith("http/") || httpResource.startsWith("https/")) => HTTPSource
      case _ => HTTPSource
    }

    def removeSourcePrefix(uri: String): String = {
      if (uri.startsWith("http/")) {
        uri.substring(5)
      } else if (uri.startsWith("https/")) {
        uri.substring(6)
      } else if (uri.startsWith("facebook/")) {
        uri.substring(9)
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

    val source = toSource(uriStr)
    val uri = removePrefix(removeSourcePrefix(removeMultipleSlashes(uriStr)))

    Resource(source, uri)
  }

}
