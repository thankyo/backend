package com.clemble.loveit.common.service

import com.mohiva.play.silhouette.api.{Logger, LoginInfo, Provider}
import com.mohiva.play.silhouette.api.util.HTTPLayer
import javax.inject.Inject
import play.api.http.Status

import scala.concurrent.{ExecutionContext, Future}

trait TwoFactoryProvider extends Provider {

  def authenticate(loginInfo: LoginInfo, token: String): Future[Boolean]

}

case class AuthyTwoFactoryProvider @Inject()(apiKey: String, client: HTTPLayer, implicit val ec: ExecutionContext) extends TwoFactoryProvider with Logger {
  override def id: String = AuthyTwoFactoryProvider.ID

  override def authenticate(loginInfo: LoginInfo, token: String): Future[Boolean] = {
    // TODO hardcoded Auth user id, would need to make it more flexible in future
    val url = s"https://api.authy.com/protected/json/verify/${token}/1945535"
    client.url(url).withHttpHeaders("X-Authy-API-Key" -> apiKey).get().map(res => {
      if (res.status == Status.OK) {
        val success = (res.json \ "success")
        success.asOpt[String].contains("true")
      } else {
        logger.error("Failed to retrieve credentials")
        false
      }
    })

  }

}

object AuthyTwoFactoryProvider {

  val ID = "authy"

}