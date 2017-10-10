package com.clemble.loveit.thank.service

import javax.inject.{Inject, Singleton}

import com.clemble.loveit.common.model.{HttpResource, Resource, UserID}
import com.clemble.loveit.thank.model.{NotVerified, VerificationStatus, Verified}

import scala.concurrent.{ExecutionContext, Future}

sealed trait ROVerificationConfirmationService[T <: Resource] {

  def confirm(user: UserID, res: T): Future[VerificationStatus]

}

case class ROVerificationConfirmationFacade(
                                             httpVerification: ROVerificationConfirmationService[HttpResource]
                                           ) extends ROVerificationConfirmationService[Resource] {

  override def confirm(user: UserID, res: Resource): Future[VerificationStatus] = {
    res match {
      case httpRes : HttpResource => httpVerification.confirm(user, httpRes)
    }
  }

}

@Singleton
case class HttpROVerificationConfirmationService @Inject()(
                                                            tagReader: MetaTagReader,
                                                            tagEncryptor: ROVerificationGenerator,
                                                            implicit val ec: ExecutionContext
                                                          ) extends ROVerificationConfirmationService[HttpResource] {

  override def confirm(user: UserID, res: HttpResource): Future[VerificationStatus] = {
    for {
      tagOpt <- tagReader.read(res)
    } yield {
      tagOpt match {
        case Some(tag) if (tagEncryptor.decrypt(tag) == (user, res)) => Verified
        case _ => NotVerified
      }
    }
  }

}