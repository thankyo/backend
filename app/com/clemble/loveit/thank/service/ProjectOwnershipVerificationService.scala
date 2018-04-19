package com.clemble.loveit.thank.service

import javax.inject.Inject
import com.clemble.loveit.common.model.{DibsVerification, Resource, UserID, Verification}

import scala.concurrent.{ExecutionContext, Future}

trait ProjectOwnershipVerificationService {

  def verify(user: UserID, url: Resource): Future[Verification]

}

case class SimpleProjectOwnershipVerificationService @Inject()(ownershipService: ProjectOwnershipService, implicit val ec: ExecutionContext) extends ProjectOwnershipVerificationService {

  override def verify(user: UserID, url: Resource): Future[Verification] = {
    ownershipService.fetch(user).map(_.find(prj => url.startsWith(prj.url)).map(_.verification).getOrElse(DibsVerification))
  }

}

case object TestProjectOwnershipVerificationService extends ProjectOwnershipVerificationService {

  override def verify(user: UserID, url: Resource): Future[Verification] = {
    Future.successful(DibsVerification)
  }

}