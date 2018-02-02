package com.clemble.loveit.thank.service

import javax.inject.{Inject, Singleton}

import com.clemble.loveit.common.error.{RepositoryException, ResourceException, UserException}
import com.clemble.loveit.common.model.{Resource, UserID}
import com.clemble.loveit.thank.model._
import com.clemble.loveit.thank.service.repository.{ROVerificationRepository}

import scala.concurrent.{ExecutionContext, Future}

/**
  * Ownership verification service
  */
trait ROVerificationService {

  def get(user: UserID): Future[Option[ROVerification[Resource]]]

  def create(user: UserID, req: Resource): Future[ROVerification[Resource]]

  def remove(user: UserID): Future[Boolean]

  def verify(user: UserID): Future[Option[ROVerification[Resource]]]

}

@Singleton
case class SimpleROVerificationService @Inject()(
                                                  generator: ROVerificationGenerator,
                                                  repo: ROVerificationRepository,
                                                  prjService: SupportedProjectService,
                                                  resOwnService: ROService,
                                                  confirmationService: ROVerificationConfirmationService[Resource],
                                                  implicit val ec: ExecutionContext
                                                ) extends ROVerificationService {
  override def get(requester: UserID): Future[Option[ROVerification[Resource]]] = {
    repo.get(requester)
  }

  override def remove(requester: UserID): Future[Boolean] = {
    repo.delete(requester)
  }

  override def create(user: UserID, res: Resource): Future[ROVerification[Resource]] = {
    val fSavedReq = for {
      prjOpt <- prjService.findProject(res)
    } yield {
      if (prjOpt.isDefined)
        throw UserException.resourceAlreadyOwned(prjOpt.get.user)
      repo.save(user, generator.generate(user, res)).recoverWith({
        case RepositoryException(RepositoryException.DUPLICATE_KEY_CODE, _) =>
          Future.failed(ResourceException.verificationAlreadyRequested())
      })
    }
    fSavedReq.flatMap(f => f)
  }


  override def verify(requester: UserID): Future[Option[ROVerification[Resource]]] = {
    for {
      verOpt <- repo.get(requester)
      res = verOpt.get.resource
      statusUpdate <- confirmationService.confirm(requester, res)
      updated <- repo.update(requester, res, statusUpdate)
    } yield {
      if (!updated)
        throw new IllegalArgumentException("Internal problem")
      if (statusUpdate == Verified)
        resOwnService.validate(SupportedProject(res, requester))

      verOpt.map(_.copy(status = statusUpdate))
    }
  }

}