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

case class EmailProjectOwnershipToken(
  user: UserID,
  email: Email,
  url: Resource,
  token: UUID = UUID.randomUUID(),
  created: LocalDateTime = LocalDateTime.now()
) extends Token with ResourceAware

object EmailProjectOwnershipToken {

  implicit val json: OFormat[EmailProjectOwnershipToken] = Json.format[EmailProjectOwnershipToken]

}

trait EmailProjectOwnershipService extends ProjectOwnershipService {

  def verifyWithWHOIS(user: UserID, url: Resource): Future[Option[EmailProjectOwnershipToken]]

  def verifyWithDomainEmail(user: UserID, email: String, url: Resource): Future[EmailProjectOwnershipToken]

  def validate(user: UserID, token: UUID): Future[Option[EmailProjectOwnershipToken]]

}

@Singleton
case class SimpleEmailProjectOwnershipService @Inject()(
  whoisService: WHOISService,
  emailService: EmailService,
  repo: TokenRepository[EmailProjectOwnershipToken]
)(implicit ec: ExecutionContext) extends EmailProjectOwnershipService {

  private def sendEmailAndUpdateProject(user: UserID, email: Email, url: Resource): Future[EmailProjectOwnershipToken] = {
    for {
      token <- repo.save(EmailProjectOwnershipToken(user, email, url))
      emailSent <- emailService.sendDomainVerificationEmail(email, token)
    } yield {
      if (!emailSent) throw new IllegalArgumentException("Failed to send email")
      token
    }
  }

  override def verifyWithWHOIS(user: UserID, url: Resource): Future[Option[EmailProjectOwnershipToken]] = {
    whoisService.fetchEmail(url).flatMap({
      case Some(email) => sendEmailAndUpdateProject(user, email, url).map(Some(_))
      case None => Future.successful(None)
    })
  }

  override def verifyWithDomainEmail(user: UserID, email: Email, url: Resource): Future[EmailProjectOwnershipToken] = {
    val emailDomain = email.toEmailDomain()
    val resDomain = url.toParentDomain()
    if (emailDomain != resDomain) {
      throw new FieldValidationError("email", s"Domain does not match ${url}")
    }
    sendEmailAndUpdateProject(user, email, url)
  }

  override def validate(user: UserID, token: UUID): Future[Option[EmailProjectOwnershipToken]] = {
    repo.findAndRemoveByToken(token).map({
      case Some(verification) if verification.user != user =>
        throw new IllegalArgumentException("Token was created by different user")
      case verificationOpt => verificationOpt
    })
  }

}