package com.clemble.loveit.thank.service

import java.time.{LocalDate, LocalDateTime}
import java.util.UUID

import com.clemble.loveit.common.model.{CreatedAware, Email, Project, Resource, ResourceAware, TokenAware, UserAware, UserID}
import play.api.libs.json.Json

import scala.concurrent.Future

case class EmailVerificationToken(
  user: UserID,
  email: Email,
  token: UUID,
  created: LocalDateTime
) extends TokenAware

object EmailVerificationToken {

  implicit val json = Json.format[EmailVerificationToken]

}

case class EmailVerificationRequest(
  email: String,
  url: Resource
) extends ResourceAware

case class EmailVerification(
  user: UserID,
  url: Resource,
  email: String,
  token: String
) extends ResourceAware with UserAware

trait ProjectEmailVerificationService {

  def verify(verificationRequest: EmailVerificationRequest): Future[EmailVerification]

}
