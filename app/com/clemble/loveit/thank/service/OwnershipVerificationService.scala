package com.clemble.loveit.thank.service

import javax.inject.{Inject, Singleton}

import com.clemble.loveit.common.model.{HttpResource, SocialResource}
import com.clemble.loveit.thank.model.{NonVerified, OwnershipVerificationRequest, Verified}
import com.clemble.loveit.thank.service.repository.OwnershipVerificationRepository
import play.api.libs.ws.WSClient

import scala.concurrent.{ExecutionContext, Future}

/**
  * Ownership verification service
  */
trait OwnershipVerificationService {
  def verify(ownershipRequest: OwnershipVerificationRequest): Future[OwnershipVerificationRequest]
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
case class WSMetaTagReader @Inject() (wsClient: WSClient, implicit val ec: ExecutionContext) extends MetaTagReader {

  def read(res: HttpResource): Future[Option[String]] = {
    for {
      resp <- wsClient.url(res.uri).execute()
    } yield {
      MetaTagReader.findInHtml(resp.body)
    }
  }

}

@Singleton
case class HttpOwnershipVerificationService @Inject()(tagReader: MetaTagReader, repo: OwnershipVerificationRepository, resOwnService: ResourceOwnershipService, implicit val ec: ExecutionContext) extends OwnershipVerificationService {

  override def verify(req: OwnershipVerificationRequest): Future[OwnershipVerificationRequest] = {
    req.resource match {
      case  res @ HttpResource(uri) =>
        for {
          tagOpt <- tagReader.read(res)
        } yield {
          tagOpt match {
            case Some(tag) if (tag == req.verificationCode) =>
              resOwnService.assign(req.requester, req.toOwnership())
              repo.update(req, Verified)
            case _ =>
              repo.update(req, NonVerified)
          }
        }
      case SocialResource(_, _) =>
        throw new IllegalArgumentException("Do not support Social resources")
    }
    repo.save(req)
  }

}