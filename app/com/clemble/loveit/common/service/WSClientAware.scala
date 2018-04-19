package com.clemble.loveit.common.service

import com.clemble.loveit.common.model.Resource
import play.api.http.Status
import play.api.libs.ws.WSClient

import scala.concurrent.{ExecutionContext, Future}

trait WSClientAware {

  val client: WSClient

}


object WSClientAware {

  implicit class ExtendedWSClient(client: WSClient) {

    def isAlive(url: Resource)(implicit ec: ExecutionContext): Future[Boolean] = {
      client.url(url)
        .withFollowRedirects(false)
        .get()
        .filter(res => res.status == Status.OK)
        .map(_ => true)
        .recover({ case _ => false })
    }

  }

}