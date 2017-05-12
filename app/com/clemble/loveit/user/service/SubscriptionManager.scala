package com.clemble.loveit.user.service

import javax.inject.{Inject, Singleton}
import com.clemble.loveit.user.model.User
import com.mohiva.play.silhouette.api.Logger
import play.api.libs.ws.WSAuthScheme.BASIC
import play.api.libs.ws.WSClient

import scala.concurrent.{ExecutionContext, Future}

sealed trait SubscriptionManager {
  def signedUp(user: User): Future[Boolean]
}

case object TestSubscriptionManager extends SubscriptionManager {
  override def signedUp(user: User): Future[Boolean] = Future.successful(true)
}


@Singleton
case class MailgunSubscriptionManager @Inject()(apiKey: String, ws: WSClient, implicit val ec: ExecutionContext) extends SubscriptionManager with Logger {

  private def asMailgunEmailForm(user: User): Option[Map[String, Seq[String]]] = {
    user.email.map(address => {
      Map(
        "address" -> Seq(address),
        "subscribed" -> Seq("true"),
        "name" -> Seq(s"${user.firstName.getOrElse("")} ${user.lastName.getOrElse("")}".trim())
      )
    })
  }

  private def subscribe(list: String, user: User): Future[Boolean] = {
    asMailgunEmailForm(user) match {
      case Some(emailForm) =>
        val uri = s"https://api.mailgun.net/v3/lists/${list}@mailgun.loveit.tips/members"
        ws.
          url(uri).
          withAuth("api", apiKey, BASIC).
          post(emailForm).
          map(res => {
            val isValid = 200 <= res.status && res.status < 400
            if (!isValid)
              logger.error(s"Failed to add user ${user.id}, because of ${res.body}")
            isValid
          }).
          recover({ case t => {
            logger.error(s"Failed to add ${user.email.get} to ${list}", t)
            false
          }})
      case None =>
        Future.successful(true)
    }
  }

  override def signedUp(user: User): Future[Boolean] = {
    subscribe("users", user)
  }

}
