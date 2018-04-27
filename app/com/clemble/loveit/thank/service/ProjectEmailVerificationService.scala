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

case class ProjectOwnershipByEmailToken(
  user: UserID,
  email: Email,
  url: Resource,
  token: UUID = UUID.randomUUID(),
  created: LocalDateTime = LocalDateTime.now()
) extends Token with ResourceAware

object ProjectOwnershipByEmailToken {

  implicit val json: OFormat[ProjectOwnershipByEmailToken] = Json.format[ProjectOwnershipByEmailToken]

}

trait ProjectOwnershipByEmailService extends ProjectOwnershipService {

  def verifyWithWHOIS(user: UserID, url: Resource): Future[Option[ProjectOwnershipByEmailToken]]

  def verifyWithDomainEmail(user: UserID, email: String, url: Resource): Future[ProjectOwnershipByEmailToken]

  def validate(user: UserID, token: UUID): Future[Option[ProjectOwnershipByEmailToken]]

}

@Singleton
case class SimpleProjectOwnershipByEmailService @Inject()(
  whoisService: WHOISService,
  emailService: EmailService,
  repo: TokenRepository[ProjectOwnershipByEmailToken]
)(implicit ec: ExecutionContext) extends ProjectOwnershipByEmailService {

  private def sendEmailAndUpdateProject(user: UserID, email: Email, url: Resource): Future[ProjectOwnershipByEmailToken] = {
    for {
      token <- repo.save(ProjectOwnershipByEmailToken(user, email, url))
      emailSent <- emailService.sendDomainVerificationEmail(email, token)
    } yield {
      if (!emailSent) throw new IllegalArgumentException("Failed to send email")
      token
    }
  }

  override def verifyWithWHOIS(user: UserID, url: Resource): Future[Option[ProjectOwnershipByEmailToken]] = {
    whoisService.fetchEmail(url).flatMap({
      case Some(email) => sendEmailAndUpdateProject(user, email, url).map(Some(_))
      case None => Future.successful(None)
    })
  }

  override def verifyWithDomainEmail(user: UserID, email: Email, url: Resource): Future[ProjectOwnershipByEmailToken] = {
    val emailDomain = email.toEmailDomain()
    val resDomain = url.toParentDomain()
    if (emailDomain != resDomain) {
      throw new FieldValidationError("email", s"Domain does not match ${url}")
    }
    sendEmailAndUpdateProject(user, email, url)
  }

  override def validate(user: UserID, token: UUID): Future[Option[ProjectOwnershipByEmailToken]] = {
    repo.findAndRemoveByToken(token).map({
      case Some(verification) if verification.user != user =>
        throw new IllegalArgumentException("Token was created by different user")
      case verificationOpt => verificationOpt
    })
  }

}