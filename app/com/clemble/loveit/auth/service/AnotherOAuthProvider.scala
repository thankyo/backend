package com.clemble.loveit.auth.service

import java.io.IOException
import java.net.{HttpURLConnection, MalformedURLException, URL}

import play.shaded.oauth.oauth.signpost.AbstractOAuthProvider
import play.shaded.oauth.oauth.signpost.basic.{HttpURLConnectionRequestAdapter, HttpURLConnectionResponseAdapter}
import play.shaded.oauth.oauth.signpost.http.{HttpRequest, HttpResponse}

class AnotherOAuthProvider(requestTokenEndpointUrl: String, accessTokenEndpointUrl: String, authorizationWebsiteUrl: String)
  extends AbstractOAuthProvider(requestTokenEndpointUrl, accessTokenEndpointUrl, authorizationWebsiteUrl) {

  @throws[MalformedURLException]
  @throws[IOException]
  override protected def createRequest(endpointUrl: String): HttpRequest = {
    val connection = new URL(endpointUrl).openConnection.asInstanceOf[HttpURLConnection]
    connection.setRequestMethod("POST")
    connection.setAllowUserInteraction(false)
    connection.setRequestProperty("Transfer-Encoding", "chunked")
    connection.setRequestProperty("Content-Length", "0")
    connection.setRequestProperty("ContentLength", "0")
    new HttpURLConnectionRequestAdapter(connection)
  }

  @throws[IOException]
  override protected def sendRequest(request: HttpRequest): HttpResponse = {
    val connection = request.unwrap.asInstanceOf[HttpURLConnection]
    connection.connect()
    new HttpURLConnectionResponseAdapter(connection)
  }

  override protected def closeConnection(request: HttpRequest, response: HttpResponse): Unit = {
    val connection = request.unwrap.asInstanceOf[HttpURLConnection]
    if (connection != null) connection.disconnect()
  }

}
