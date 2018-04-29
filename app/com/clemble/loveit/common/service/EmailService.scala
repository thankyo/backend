package com.clemble.loveit.common.service

import com.clemble.loveit.auth.model.ResetPasswordToken
import com.clemble.loveit.common.model.{Email}
import com.clemble.loveit.thank.service.{DibsProjectOwnershipToken, EmailProjectOwnershipToken}
import com.mohiva.play.silhouette.api.Logger
import org.matthicks.mailgun.{EmailAddress, Mailgun, Message}
import play.api.i18n.{I18nSupport, Lang, Messages, MessagesApi}

import scala.concurrent.{ExecutionContext, Future}

trait EmailService {

  def sendResetPasswordEmail(email: Email, authToken: ResetPasswordToken): Future[Boolean]

  def sendWHOISVerificationEmail(dibsToken: DibsProjectOwnershipToken): Future[Boolean]

  def sendDomainVerificationEmail(emailToken: EmailProjectOwnershipToken): Future[Boolean]

}

case class MailgunEmailService(mailgun: Mailgun)(implicit val messagesApi: MessagesApi, ec: ExecutionContext) extends EmailService with I18nSupport {
  // TODO this does not really work with internationalization
  implicit val messageProvider: Messages = messagesApi.preferred(Seq(Lang.defaultLang))

  override def sendResetPasswordEmail(email: Email, authToken: ResetPasswordToken): Future[Boolean] = {
    val url = s"https://loveit.tips/auth/reset/${authToken.token}"
    mailgun.send(
      Message.simple(
        subject = "Reset password",
        from = EmailAddress("antono@loveit.tips", "Love it"),
        to = EmailAddress(email),
        text =
          s"""
            |<html>
            |<body>
            |  <p>Reset password was requested</p>
            |  <p>Navigate this link to reset your password</p>
            |  <p>${url}</p>
            |</body>
            |</html>
          """.stripMargin,
        html =
          s"""
            |Reset password was requested
            |Navigate this link to reset your password
            |${url}
          """.stripMargin
      )
    ).map(_ => true)
  }


  override def sendWHOISVerificationEmail(dibsToken: DibsProjectOwnershipToken): Future[Boolean] = {
    val url = s"https://loveit.tips/creator/dibs/verify/${dibsToken.token}"
    mailgun.send(
      Message.simple(
        subject = s"Verify ${dibsToken.url} ownership",
        from = EmailAddress("antono@loveit.tips", "Love it"),
        to = EmailAddress(dibsToken.email),
        text =
          s"""
             |<html>
             |<body>
             |  <p>Ownership verification required for ${dibsToken.url}</p>
             |  <p>Navigate this link to verify ownership</p>
             |  <p>${url}</p>
             |</body>
             |</html>
          """.stripMargin,
        html =
          s"""
             |Ownership verification required for ${dibsToken.url}
             |Navigate this link to verify ownership
             |${url}
          """.stripMargin
      )
    ).map(_ => true)
  }

  override def sendDomainVerificationEmail(emailToken: EmailProjectOwnershipToken): Future[Boolean] = {
    val url = s"https://loveit.tips/creator/email/verify/${emailToken.token}"
    mailgun.send(
      Message.simple(
        subject = s"Verify ${emailToken.email} ownership",
        from = EmailAddress("antono@loveit.tips", "Love it"),
        to = EmailAddress(emailToken.email),
        text =
          s"""
             |<html>
             |<body>
             |  <p>Ownership verification required for ${emailToken.email}</p>
             |  <p>Navigate this link to verify ownership</p>
             |  <p>${url}</p>
             |</body>
             |</html>
          """.stripMargin,
        html =
          s"""
             |Ownership verification required for ${emailToken.email}
             |Navigate this link to verify ownership
             |${url}
          """.stripMargin
      )
    ).map(_ => true)
  }

}

class StubEmailService extends EmailService with Logger {

  override def sendResetPasswordEmail(email: Email, reset: ResetPasswordToken): Future[Boolean] = {
    logger.info(s"Restore url is ${reset.token}")
    Future.successful(true)
  }

  override def sendDomainVerificationEmail(verification: EmailProjectOwnershipToken): Future[Boolean] = {
    logger.info(s"Verify domain ownership url is ${verification.token}")
    Future.successful(true)
  }

  override def sendWHOISVerificationEmail(dibsToken: DibsProjectOwnershipToken): Future[Boolean] = {
    logger.info(s"Verify DIBS ownership for url is ${dibsToken.token}")
    Future.successful(true)
  }
}