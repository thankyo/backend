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

  private def doVerify(req: ROVerification[Resource]): Future[ROVerification[Resource]] = {
    def assignOwnershipIfVerified(status: VerificationStatus): Unit = {
      status match {
        case Verified =>
          for {
            _ <- resOwnService.assignOwnership(req.requester, req.resource)
            removed <- repo.delete(req.requester, req.id)
          } yield {
            removed
          }
        case _ =>
      }
    }

    for {
      status <- confirmationService.confirm(req.resource, req.verificationCode).recover({ case _ => NotVerified })
      updated <- repo.update(req, status)
    } yield {
      assignOwnershipIfVerified(status)

      if (updated) req.copy(status = status) else req
    }
  }

  override def verify(requester: UserID, req: VerificationID): Future[Option[ROVerification[Resource]]] = {
    get(requester, req).flatMap(_ match {
      case Some(req) => doVerify(req).map(Some(_))
      case None => Future.successful(None)
    })
  }

}