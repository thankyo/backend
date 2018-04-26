package com.clemble.loveit.thank.service

import java.time.LocalDateTime
import java.util.UUID

import com.clemble.loveit.auth.service.EmailService
import com.clemble.loveit.common.error.FieldValidationError
import com.clemble.loveit.common.model._
import com.clemble.loveit.common.service.TokenRepository
import javax.inject.{Inject, Singleton}
import play.api.libs.json.{Json, OFormat}

import scala.concurrent.{ExecutionContext, Future}

case class EmailVerificationToken(
  user: UserID,
  email: Email,
  url: Resource,
  token: UUID = UUID.randomUUID(),
  created: LocalDateTime = LocalDateTime.now()
) extends Token with ResourceAware

object EmailVerificationToken {

  implicit val json: OFormat[EmailVerificationToken] = Json.format[EmailVerificationToken]

}

trait EmailVerificationTokenService {

  def verifyWithWHOIS(user: UserID, url: Resource): Future[Option[EmailVerificationToken]]

  def verifyWithDomainEmail(user: UserID, email: String, url: Resource): Future[EmailVerificationToken]

  def validate(user: UserID, token: UUID): Future[Option[EmailVerificationToken]]

}

@Singleton
case class SimpleEmailVerificationTokenService @Inject()(
  whoisService: WHOISService,
  emailService: EmailService,
  repo: TokenRepository[EmailVerificationToken]
)(implicit ec: ExecutionContext) extends EmailVerificationTokenService {

  private def sendEmailAndUpdateProject(user: UserID, email: Email, url: Resource): Future[EmailVerificationToken] = {
    val token = EmailVerificationToken(user, email, url)
    repo.save(token)
  }

  override def verifyWithWHOIS(user: UserID, url: Resource): Future[Option[EmailVerificationToken]] = {
    whoisService.fetchEmail(url).flatMap({
      case Some(email) => sendEmailAndUpdateProject(user, email, url).map(Some(_))
      case None => Future.successful(None)
    })
  }

  override def verifyWithDomainEmail(user: UserID, email: Email, url: Resource): Future[EmailVerificationToken] = {
    val emailDomain = email.toEmailDomain()
    val resDomain = url.toParentDomain()
    if (emailDomain != resDomain) {
      throw new FieldValidationError("email", s"Domain does not match ${url}")
    }
    sendEmailAndUpdateProject(user, email, url)
  }

  override def validate(user: UserID, token: UUID): Future[Option[EmailVerificationToken]] = {
    repo.findAndRemoveByToken(token).map({
      case Some(verification) if verification.user != user =>
        throw new IllegalArgumentException("Token was created by different user")
      case verificationOpt => verificationOpt
    })
  }

}