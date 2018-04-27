package com.clemble.loveit.auth.service

import com.clemble.loveit.auth.model.ResetPasswordToken
import com.clemble.loveit.auth.views.html.emails.resetPassword
import org.matthicks.mailgun.{EmailAddress, Mailgun, Message}
import play.api.i18n.{I18nSupport, Lang, Messages, MessagesApi}
import com.clemble.loveit.auth.views.txt.emails
import com.clemble.loveit.common.model.{Email, User}
import com.clemble.loveit.thank.service.ProjectOwnershipByEmailToken
import com.mohiva.play.silhouette.api.Logger

import scala.concurrent.{ExecutionContext, Future}

trait EmailService {

  def sendResetPasswordEmail(email: Email, authToken: ResetPasswordToken): Future[Boolean]

  def sendDomainVerificationEmail(email: Email, token: ProjectOwnershipByEmailToken): Future[Boolean]

}

case class MailgunEmailService(mailgun: Mailgun)(implicit val messagesApi: MessagesApi, ec: ExecutionContext) extends EmailService with I18nSupport {
  // TODO this does not really work with internationalization
  implicit val messageProvider: Messages = messagesApi.preferred(Seq(Lang.defaultLang))

  override def sendResetPasswordEmail(email: Email, authToken: ResetPasswordToken): Future[Boolean] = {
    val url = s"https://loveit.tips/auth/reset/${authToken.token}"
    mailgun.send(
      Message.simple(
        subject = Messages("email.reset.password.subject"),
        from = EmailAddress(Messages("email.from"), "Love it"),
        to = EmailAddress(email),
        text = emails.resetPassword(url).body,
        html = resetPassword(url).body
      )
    ).map(_ => true)
  }

  override def sendDomainVerificationEmail(email: Email, token: ProjectOwnershipByEmailToken): Future[Boolean] = {
    Future.successful(true)
  }

}

class StubEmailService extends EmailService with Logger {

  override def sendResetPasswordEmail(email: Email, reset: ResetPasswordToken): Future[Boolean] = {
    val url = s"http://localhost:8080/auth/reset/${reset.token}"
    logger.info(s"Restore url is ${url}")
    Future.successful(true)
  }

  override def sendDomainVerificationEmail(email: Email, verification: ProjectOwnershipByEmailToken): Future[Boolean] = {
    val url = s"http://localhost:8080/auth/reset/${verification.token}"
    logger.info(s"Verify domain ownership url is ${url}")
    Future.successful(true)
  }

}