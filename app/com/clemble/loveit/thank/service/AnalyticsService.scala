package com.clemble.loveit.thank.service

import java.net.{URLEncoder}

import com.clemble.loveit.common.model.{Resource, UserID}
import com.google.inject.Inject
import play.api.libs.ws.WSClient

import scala.concurrent.{ExecutionContext}

trait AnalyticsService {

  def thank(user: UserID, uri: Resource): Unit

}

case object StubAnalyticsService extends AnalyticsService {

  override def thank(user: UserID, uri: Resource): Unit = {
  }

}

case class GoogleAnalyticsService @Inject()(tid: String, ws: WSClient, implicit val ec: ExecutionContext) extends AnalyticsService {

  override def thank(user: UserID, uri: Resource): Unit = {
    val event = s"v=1&tid=${tid}&uid=${user}&t=event&ec=Thank&ea=${uri}"
    ws.
      url("https://www.google-analytics.com/collect").
      post(URLEncoder.encode(event, "UTF-8"))
  }

}