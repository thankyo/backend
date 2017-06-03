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

  def get(requester: UserID, res: Resource): Future[Option[ROVerification[Resource]]]

  def list(requester: UserID): Future[Set[ROVerification[Resource]]]

  def create(requester: UserID, req: Resource): Future[ROVerification[Resource]]

  def remove(requester: UserID, res: Resource): Future[Boolean]

  def verify(requester: UserID, res: Resource): Future[Option[ROVerification[Resource]]]

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

  override def create(user: UserID, res: Resource): Future[ROVerification[Resource]] = {
    val fSavedReq = for {
      ownerOpt <- resRepo.findOwner(res)
    } yield {
      if (ownerOpt.isDefined)
        throw UserException.resourceAlreadyOwned(ownerOpt.get)
      repo.save(user, generator.generate(user, res)).recoverWith({
        case RepositoryException(Seq(RepositoryError(RepositoryError.DUPLICATE_KEY_CODE, _))) =>
          Future.failed(ResourceException.verificationAlreadyRequested())
      })
    }
    fSavedReq.flatMap(f => f)
  }

  override def remove(requester: UserID, res: Resource): Future[Boolean] = {
    repo.delete(requester, res)
  }

  override def list(requester: UserID): Future[Set[ROVerification[Resource]]] = {
    repo.list(requester)
  }

  override def get(requester: UserID, res: Resource): Future[Option[ROVerification[Resource]]] = {
    repo.get(requester, res)
  }

  override def verify(requester: UserID, res: Resource): Future[Option[ROVerification[Resource]]] = {
    val fVerification = for {
      statusUpdate <- confirmationService.confirm(requester, res)
      updated <- repo.update(requester, res, statusUpdate)
    } yield {
      if (!updated)
        throw new IllegalArgumentException("Internal problem")
      if (statusUpdate == Verified)
        resOwnService.assignOwnership(requester, res)

      statusUpdate
    }

    fVerification.flatMap(_ => get(requester, res))
  }

}