package com.clemble.loveit.thank.service

import java.time.LocalDateTime
import java.util.UUID

import com.clemble.loveit.common.error.{FieldValidationError, RepositoryException}
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
) extends TokenAware with ResourceAware

object EmailVerificationToken {

  implicit val json: OFormat[EmailVerificationToken] = Json.format[EmailVerificationToken]

}

trait EmailVerificationTokenService {

  def create(user: UserID, email: String, url: Resource): Future[EmailVerificationToken]

  def validate(user: UserID, token: UUID): Future[Option[EmailVerificationToken]]

}

@Singleton
case class SimpleEmailVerificationTokenService @Inject()(repo: TokenRepository[EmailVerificationToken])(implicit ec: ExecutionContext) extends EmailVerificationTokenService {

  override def create(user: UserID, email: Email, url: Resource): Future[EmailVerificationToken] = {
    val emailDomain = email.toEmailDomain()
    val resDomain = url.toParentDomain()
    if (emailDomain != resDomain) {
      throw new FieldValidationError("email", s"Domain does not match ${url}")
    }
    val token = EmailVerificationToken(user, email, url)
    repo.save(token)
  }

  override def validate(user: UserID, token: UUID): Future[Option[EmailVerificationToken]] = {
    val findRes = repo.findByToken(token)
    repo.removeByToken(token)
    findRes.map({
      case Some(verification) if verification.user != user =>
        throw new IllegalArgumentException("Token was created by different user")
      case verificationOpt => verificationOpt
    })
  }

}