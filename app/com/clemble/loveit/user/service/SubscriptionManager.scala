package com.clemble.loveit.user.service

import javax.inject.{Inject, Singleton}
import com.clemble.loveit.user.model.User
import com.mohiva.play.silhouette.api.Logger
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


@Singleton
case class MailgunSubscriptionManager @Inject()(apiKey: String, ws: WSClient, implicit val ec: ExecutionContext) extends SubscriptionManager with Logger {

  private def remove(list: String, email: String) = {
    val uri = s"https://api.mailgun.net/v3/lists/${list}@mailgun.loveit.tips/members/${email}"
    ws.
      url(uri).
      withAuth("api", apiKey, BASIC).
      delete().
      map(_ => true).
      recover({ case _ => true })
  }

  private def subscribe(list: String, data: Map[String, Seq[String]]): Future[Boolean] = {
    val uri = s"https://api.mailgun.net/v3/lists/${list}@mailgun.loveit.tips/members"
    ws.
      url(uri).
      withAuth("api", apiKey, BASIC).
      post(data).
      map(res => {
        val isValid = 200 <= res.status && res.status < 400
        if (!isValid)
          logger.error(s"Failed to add user, because of ${res.body}")
        isValid
      }).
      recover({ case t => {
        logger.error(s"Failed to add to ${list}", t)
        false
      }})
  }

  private def asMailgunEmailForm(user: User): Option[Map[String, Seq[String]]] = {
    user.email.map(address => {
      Map(
        "address" -> Seq(address),
        "subscribed" -> Seq("true"),
        "name" -> Seq(s"${user.firstName.getOrElse("")} ${user.lastName.getOrElse("")}".trim())
      )
    })
  }

  private val USERS_LIST = "users"
  private val CREATORS_LIST = "creatorleads"
  private val CONTRIBUTOR_LIST = "contributorleads"

  override def signedUpUser(user: User): Future[Boolean] = {
    asMailgunEmailForm(user) match {
      case Some(emailForm) =>
        subscribe(USERS_LIST, emailForm)
        remove(CREATORS_LIST, user.email.get)
        remove(CONTRIBUTOR_LIST, user.email.get)
      case None =>
        Future.successful(true)
    }
  }

  override def subscribeCreator(email: String): Future[Boolean] = {
    subscribe(CREATORS_LIST, Map("address" -> Seq(email)))
  }

  override def subscribeContributor(email: String): Future[Boolean] = {
    subscribe(CONTRIBUTOR_LIST, Map("address" -> Seq(email)))
  }
}
