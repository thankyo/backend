package com.clemble.loveit.thank.service

import javax.inject.{Inject, Singleton}

import com.clemble.loveit.common.model.{HttpResource, Resource, UserID}
import com.clemble.loveit.thank.model._
import com.clemble.loveit.thank.service.repository.OwnershipVerificationRepository
import play.api.libs.ws.WSClient

import scala.concurrent.{ExecutionContext, Future}

/**
  * Ownership verification service
  */
trait OwnershipVerificationService {

  def get(requester: UserID, req: VerificationID): Future[Option[OwnershipVerificationRequest[Resource]]]

  def list(requester: UserID): Future[Set[OwnershipVerificationRequest[Resource]]]

  def create(requester: UserID, ownership: ResourceOwnership): Future[OwnershipVerificationRequest[Resource]]

  def remove(requester: UserID, req: VerificationID): Future[Boolean]

  def verify(requester: UserID, req: VerificationID): Future[Option[OwnershipVerificationRequest[Resource]]]

}

trait MetaTagReader {
  def read(res: HttpResource): Future[Option[String]]
}

object MetaTagReader {

  private val META_DESCRIPTION = """.*meta\s+name="loveit-site-verification"\s*content="([^"]+)"""".r

  def findInHtml(html: String): Option[String] = {
    META_DESCRIPTION.findFirstMatchIn(html).map(_.group(1))
  }

}

@Singleton
case class WSMetaTagReader @Inject()(wsClient: WSClient, implicit val ec: ExecutionContext) extends MetaTagReader {

  def read(res: HttpResource): Future[Option[String]] = {
    for {
      resp <- wsClient.url(s"http://${res.uri}").execute()
    } yield {
      MetaTagReader.findInHtml(resp.body)
    }
  }

}

@Singleton
case class SimpleOwnershipVerificationService @Inject()(generator: OwnershipVerificationGenerator, repo: OwnershipVerificationRepository, resOwnService: ResourceOwnershipService, verificationService: ResourceVerificationService[Resource], implicit val ec: ExecutionContext) extends OwnershipVerificationService {

  override def create(requester: UserID, ownership: ResourceOwnership): Future[OwnershipVerificationRequest[Resource]] = {
    val req = generator.generate(requester, ownership)
    repo.save(req)
  }

  override def remove(requester: UserID, req: VerificationID): Future[Boolean] = {
    repo.delete(requester, req)
  }

  override def list(requester: UserID): Future[Set[OwnershipVerificationRequest[Resource]]] = {
    repo.list(requester)
  }

  override def get(requester: UserID, req: VerificationID): Future[Option[OwnershipVerificationRequest[Resource]]] = {
    repo.get(requester, req)
  }

  private def doVerify(req: OwnershipVerificationRequest[Resource]): Future[OwnershipVerificationRequest[Resource]] = {
    def assignOwnershipIfVerified(status: OwnershipVerificationRequestStatus): Unit = {
      status match {
        case Verified =>
          for {
            _ <- resOwnService.assign(req.requester, req.toOwnership())
            removed <- repo.delete(req.requester, req.id)
          } yield {
            removed
          }
        case _ =>
      }
    }

    for {
      status <- verificationService.verify(req.resource, req.verificationCode).recover({ case _ => NonVerified })
      updated <- repo.update(req, status)
    } yield {
      assignOwnershipIfVerified(status)

      if (updated) req.copy(status = status) else req
    }
  }

  override def verify(requester: UserID, req: VerificationID): Future[Option[OwnershipVerificationRequest[Resource]]] = {
    get(requester, req).flatMap(_ match {
      case Some(req) => doVerify(req).map(Some(_))
      case None => Future.successful(None)
    })
  }

}

trait ResourceVerificationService[T <: Resource] {

  def verify(resource: T, verificationCode: String): Future[OwnershipVerificationRequestStatus]

}

case class ResourceVerificationFacade(httpVerification: ResourceVerificationService[HttpResource]) extends ResourceVerificationService[Resource] {
  override def verify(resource: Resource, verificationCode: String): Future[OwnershipVerificationRequestStatus] = {
    resource match {
      case httpRes: HttpResource => httpVerification.verify(httpRes, verificationCode)
      case _ => Future.successful(NonVerified)
    }
  }
}

@Singleton
case class HttpOwnershipVerificationService @Inject()(tagReader: MetaTagReader, implicit val ec: ExecutionContext) extends ResourceVerificationService[HttpResource] {

  override def verify(res: HttpResource, verificationCode: String): Future[OwnershipVerificationRequestStatus] = {
    for {
      tagOpt <- tagReader.read(res)
    } yield {
      tagOpt match {
        case Some(tag) if (tag == verificationCode) => Verified
        case _ => NonVerified
      }
    }
  }

}