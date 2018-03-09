package com.clemble.loveit.thank.service

import javax.inject.Inject

import com.clemble.loveit.common.model.{Resource, UserID}

import scala.concurrent.{ExecutionContext, Future}

trait ProjectOwnershipVerificationService {

  def verify(user: UserID, url: Resource): Future[Boolean]

}

case class SimpleProjectOwnershipVerificationService @Inject()(ownershipService: ProjectOwnershipService, implicit val ec: ExecutionContext) extends ProjectOwnershipVerificationService {

  override def verify(user: UserID, url: Resource): Future[Boolean] = {
    ownershipService.fetch(user).map(_.exists(res => url.startsWith(res)))
  }

}

case object TestProjectOwnershipVerificationService extends ProjectOwnershipVerificationService {
  override def verify(user: UserID, url: Resource): Future[Boolean] = Future.successful(true)
}