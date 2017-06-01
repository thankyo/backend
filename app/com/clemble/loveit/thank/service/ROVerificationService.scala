package com.clemble.loveit.thank.service

import javax.inject.{Inject, Singleton}

import com.clemble.loveit.common.model.{HttpResource, Resource, UserID}
import com.clemble.loveit.thank.model._
import com.clemble.loveit.thank.service.repository.ROVerificationRepository
import play.api.libs.ws.WSClient

import scala.concurrent.{ExecutionContext, Future}

/**
  * Ownership verification service
  */
trait ROVerificationService {

  def get(requester: UserID, req: VerificationID): Future[Option[ROVerificationRequest[Resource]]]

  def list(requester: UserID): Future[Set[ROVerificationRequest[Resource]]]

  def create(requester: UserID, ownership: Resource): Future[ROVerificationRequest[Resource]]

  def remove(requester: UserID, req: VerificationID): Future[Boolean]

  def verify(requester: UserID, req: VerificationID): Future[Option[ROVerificationRequest[Resource]]]

}

@Singleton
case class SimpleROVerificationService @Inject()(generator: ROVerificationGenerator, repo: ROVerificationRepository, resOwnService: ResourceOwnershipService, confirmationService: ROVerificationConfirmationService[Resource], implicit val ec: ExecutionContext) extends ROVerificationService {

  override def create(requester: UserID, ownership: Resource): Future[ROVerificationRequest[Resource]] = {
    val req = generator.generate(requester, ownership)
    repo.save(req)
  }

  override def remove(requester: UserID, req: VerificationID): Future[Boolean] = {
    repo.delete(requester, req)
  }

  override def list(requester: UserID): Future[Set[ROVerificationRequest[Resource]]] = {
    repo.list(requester)
  }

  override def get(requester: UserID, req: VerificationID): Future[Option[ROVerificationRequest[Resource]]] = {
    repo.get(requester, req)
  }

  private def doVerify(req: ROVerificationRequest[Resource]): Future[ROVerificationRequest[Resource]] = {
    def assignOwnershipIfVerified(status: ROVerificationRequestStatus): Unit = {
      status match {
        case Verified =>
          for {
            _ <- resOwnService.assign(req.requester, req.resource)
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

  override def verify(requester: UserID, req: VerificationID): Future[Option[ROVerificationRequest[Resource]]] = {
    get(requester, req).flatMap(_ match {
      case Some(req) => doVerify(req).map(Some(_))
      case None => Future.successful(None)
    })
  }

}