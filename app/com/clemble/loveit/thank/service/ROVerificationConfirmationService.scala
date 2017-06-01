package com.clemble.loveit.thank.service

import javax.inject.{Inject, Singleton}

import com.clemble.loveit.common.model.{HttpResource, Resource}
import com.clemble.loveit.thank.model.{NotVerified, VerificationStatus, Verified}

import scala.concurrent.{ExecutionContext, Future}

sealed trait ROVerificationConfirmationService[T <: Resource] {

  def confirm(resource: T, verificationCode: String): Future[VerificationStatus]

}

case class ROVerificationConfirmationFacade(httpVerification: ROVerificationConfirmationService[HttpResource]) extends ROVerificationConfirmationService[Resource] {
  override def confirm(resource: Resource, verificationCode: String): Future[VerificationStatus] = {
    resource match {
      case httpRes: HttpResource => httpVerification.confirm(httpRes, verificationCode)
      case _ => Future.successful(NotVerified)
    }
  }
}

@Singleton
case class HttpROVerificationConfirmationService @Inject()(tagReader: MetaTagReader, implicit val ec: ExecutionContext) extends ROVerificationConfirmationService[HttpResource] {

  override def confirm(res: HttpResource, verificationCode: String): Future[VerificationStatus] = {
    for {
      tagOpt <- tagReader.read(res)
    } yield {
      tagOpt match {
        case Some(tag) if (tag == verificationCode) => Verified
        case _ => NotVerified
      }
    }
  }

}