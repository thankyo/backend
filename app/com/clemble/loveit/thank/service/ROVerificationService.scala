package com.clemble.loveit.thank.service

import javax.inject.{Inject, Singleton}

import com.clemble.loveit.common.error.{RepositoryError, RepositoryException, ResourceException, UserException}
import com.clemble.loveit.common.model.{Resource, UserID}
import com.clemble.loveit.thank.model._
import com.clemble.loveit.thank.service.repository.{ROVerificationRepository, ResourceRepository}

import scala.concurrent.{ExecutionContext, Future}

/**
  * Ownership verification service
  */
trait ROVerificationService {

  def get(requester: UserID, req: VerificationID): Future[Option[ROVerification[Resource]]]

  def list(requester: UserID): Future[Set[ROVerification[Resource]]]

  def create(requester: UserID, req: Resource): Future[ROVerification[Resource]]

  def remove(requester: UserID, req: VerificationID): Future[Boolean]

  def verify(requester: UserID, req: VerificationID): Future[Option[ROVerification[Resource]]]

}

@Singleton
case class SimpleROVerificationService @Inject()(
                                                  generator: ROVerificationGenerator,
                                                  repo: ROVerificationRepository,
                                                  resRepo: ResourceRepository,
                                                  resOwnService: ResourceOwnershipService,
                                                  confirmationService: ROVerificationConfirmationService[Resource],
                                                  implicit val ec: ExecutionContext
                                                ) extends ROVerificationService {

  override def create(requester: UserID, res: Resource): Future[ROVerification[Resource]] = {
    val fSavedReq = for {
      ownerOpt <- resRepo.findOwner(res)
    } yield {
      if (ownerOpt.isDefined)
        throw UserException.resourceAlreadyOwned(ownerOpt.get)
      repo.save(generator.generate(requester, res)).recoverWith({
        case RepositoryException(Seq(RepositoryError(RepositoryError.DUPLICATE_KEY_CODE, _))) =>
          Future.failed(ResourceException.verificationAlreadyRequested())
      })
    }
    fSavedReq.flatMap(f => f)
  }

  override def remove(requester: UserID, req: VerificationID): Future[Boolean] = {
    repo.delete(requester, req)
  }

  override def list(requester: UserID): Future[Set[ROVerification[Resource]]] = {
    repo.list(requester)
  }

  override def get(requester: UserID, req: VerificationID): Future[Option[ROVerification[Resource]]] = {
    repo.get(requester, req)
  }

  override def verify(requester: UserID, req: VerificationID): Future[Option[ROVerification[Resource]]] = {
    for {
      verOpt <- get(requester, req)
      statusUpdate <- verOpt.
        map(confirmationService.confirm).
        getOrElse(Future.successful(NotVerified)).
        recover({ case _ => NotVerified })
    } yield {
      for {
        ver <- verOpt
      } yield {
        repo.update(ver, statusUpdate)
        if (statusUpdate == Verified) {
          resOwnService.assignOwnership(requester, ver.resource)
        }
      }

      verOpt.map(_.copy(status = statusUpdate))
    }
  }

}