package com.clemble.loveit.thank.service

import javax.inject.{Inject, Singleton}

import com.clemble.loveit.common.model.{HttpResource, Resource}
import com.clemble.loveit.thank.model.{NotVerified, ROVerification, VerificationStatus, Verified}

import scala.concurrent.{ExecutionContext, Future}

sealed trait ROVerificationConfirmationService[T <: Resource] {

  def confirm(verif: ROVerification[T]): Future[VerificationStatus]

}

case class ROVerificationConfirmationFacade(httpVerification: ROVerificationConfirmationService[HttpResource]) extends ROVerificationConfirmationService[Resource] {

  override def confirm(verif: ROVerification[Resource]): Future[VerificationStatus] = {
    verif.resource match {
      case HttpResource(_) => httpVerification.confirm(verif.asInstanceOf[ROVerification[HttpResource]])
    }
  }

}

@Singleton
case class HttpROVerificationConfirmationService @Inject()(tagReader: MetaTagReader, implicit val ec: ExecutionContext) extends ROVerificationConfirmationService[HttpResource] {

  override def confirm(verif: ROVerification[HttpResource]): Future[VerificationStatus] = {
    doConfirm(verif.resource, verif.verificationCode)
  }

  private def doConfirm(res: HttpResource, verificationCode: String): Future[VerificationStatus] = {
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