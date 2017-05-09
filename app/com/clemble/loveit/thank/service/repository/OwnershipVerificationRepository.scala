package com.clemble.loveit.thank.service.repository

import com.clemble.loveit.common.model.{Resource, UserID}
import com.clemble.loveit.thank.model.{OwnershipVerificationRequest, OwnershipVerificationRequestStatus, VerificationID}

import scala.concurrent.Future

trait OwnershipVerificationRepository {

  def get(requester: UserID, request: VerificationID): Future[Option[OwnershipVerificationRequest[Resource]]]

  def list(requester: UserID): Future[Set[OwnershipVerificationRequest[Resource]]]

  def delete(requester: UserID, request: VerificationID): Future[Boolean]

  def save(req: OwnershipVerificationRequest[Resource]): Future[OwnershipVerificationRequest[Resource]]

  def update(req: OwnershipVerificationRequest[Resource], status: OwnershipVerificationRequestStatus): Future[Boolean]

}


