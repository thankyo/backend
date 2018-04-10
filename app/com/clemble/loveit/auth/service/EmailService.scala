package com.clemble.loveit.auth.service

import com.clemble.loveit.auth.model.ResetPasswordToken
import com.clemble.loveit.auth.views.html.emails.resetPassword
import org.matthicks.mailgun.{EmailAddress, Mailgun, Message}
import play.api.i18n.{I18nSupport, Messages, MessagesApi, MessagesProvider}
import com.clemble.loveit.auth.views.txt.emails
import com.clemble.loveit.common.model.User
import play.api.mvc.{Request, RequestHeader}

import scala.concurrent.{ExecutionContext, Future}

trait EmailService {

  def sendResetPasswordEmail(user: User, authToken: ResetPasswordToken)(implicit req: RequestHeader): Future[Boolean]

}

case class MailgunEmailService(mailgun: Mailgun)(implicit val messagesApi: MessagesApi, ec: ExecutionContext) extends EmailService with I18nSupport {

  override def sendResetPasswordEmail(user: User, authToken: ResetPasswordToken)(implicit req: RequestHeader): Future[Boolean] = {
    val url = s"https://loveit.tips/auth/reset/${authToken.token}"
    mailgun.send(
      Message.simple(
        subject = Messages("email.reset.password.subject"),
        from = EmailAddress(Messages("email.from"), "Love it"),
        to = EmailAddress(user.email),
        text = emails.resetPassword(user, url).body,
        html = resetPassword(user, url).body
      )
    ).map(_ => true)
  }

}

class StubEmailService extends EmailService {

  override def sendResetPasswordEmail(user: User, authToken: ResetPasswordToken)(implicit req: RequestHeader): Future[Boolean] = {
    val url = s"http://localhost:8080/auth/reset/${authToken.token}"
    println(s"Restore url is ${url}")
    Future.successful(true)
  }

}