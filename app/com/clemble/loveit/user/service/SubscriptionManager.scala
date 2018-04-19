package com.clemble.loveit.user.service

import akka.actor.{Actor, Props}
import com.clemble.loveit.common.model.User
import com.clemble.loveit.common.service.WSClientAware
import com.clemble.loveit.common.util.EventBusManager
import com.mohiva.play.silhouette.api.{Logger, SignUpEvent}
import javax.inject.{Inject, Singleton}
import play.api.libs.ws.WSAuthScheme.BASIC
import play.api.libs.ws.WSClient

import scala.concurrent.{ExecutionContext, Future}


sealed trait SubscriptionManager {

  def signedUpUser(user: User): Future[Boolean]

  def subscribeCreator(email: String): Future[Boolean]

  def subscribeContributor(email: String): Future[Boolean]
}

case object TestSubscriptionManager extends SubscriptionManager {
  override def signedUpUser(user: User): Future[Boolean] = Future.successful(true)

  override def subscribeCreator(email: String): Future[Boolean] = Future.successful(true)

  override def subscribeContributor(email: String): Future[Boolean] = Future.successful(true)
}

case class SubscriptionOnSignUpListener(subscriptionManager: SubscriptionManager) extends Actor with Logger {

  override def receive: Receive = {
    case SignUpEvent(user: User, _) =>
      subscriptionManager.signedUpUser(user)
  }

}

@Singleton
case class MailgunSubscriptionManager @Inject()(
  apiKey: String,
  client: WSClient,
  eventBusManager: EventBusManager,
  implicit val ec: ExecutionContext
) extends SubscriptionManager with Logger with WSClientAware {

  eventBusManager.onSignUp(Props(SubscriptionOnSignUpListener(this)))

  private def remove(list: String, email: String) = {
    val uri = s"https://api.mailgun.net/v3/lists/${list}@mailgun.loveit.tips/members/${email}"
    client.
      url(uri).
      withAuth("api", apiKey, BASIC).
      delete().
      map(_ => true).
      recover({ case _ => true })
  }

  private def subscribe(list: String, data: Map[String, Seq[String]]): Future[Boolean] = {
    val uri = s"https://api.mailgun.net/v3/lists/${list}@mailgun.loveit.tips/members"
    client.
      url(uri).
      withAuth("api", apiKey, BASIC).
      post(data).
      map(res => {
        val isStatusValid = 200 <= res.status && res.status < 400
        if (!isStatusValid && !res.body.contains("Address already exists")) {
          logger.error(s"Failed to add user, because of ${res.body}")
          false
        } else {
          true
        }
      }).
      recover({
        case t =>
          logger.error(s"Failed to add to ${list}", t)
          false
      })
  }

  private def asMailgunEmailForm(user: User): Map[String, Seq[String]] = {
    Map(
      "address" -> Seq(user.email),
      "subscribed" -> Seq("true"),
      "name" -> Seq(s"${user.firstName.getOrElse("")} ${user.lastName.getOrElse("")}".trim())
    )
  }

  private val USERS_LIST = "users"
  private val CREATORS_LIST = "creatorleads"
  private val CONTRIBUTOR_LIST = "contributorleads"

  override def signedUpUser(user: User): Future[Boolean] = {
    val emailForm = asMailgunEmailForm(user)
    subscribe(USERS_LIST, emailForm)
    remove(CREATORS_LIST, user.email)
    remove(CONTRIBUTOR_LIST, user.email)
  }

  override def subscribeCreator(email: String): Future[Boolean] = {
    subscribe(CREATORS_LIST, Map("address" -> Seq(email)))
  }

  override def subscribeContributor(email: String): Future[Boolean] = {
    subscribe(CONTRIBUTOR_LIST, Map("address" -> Seq(email)))
  }
}
