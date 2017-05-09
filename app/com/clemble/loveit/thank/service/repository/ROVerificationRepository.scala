package com.clemble.loveit.thank.service.repository

import com.clemble.loveit.common.model.{Resource, UserID}
import com.clemble.loveit.thank.model.{ROVerificationRequest, ROVerificationRequestStatus, VerificationID}

import scala.concurrent.Future

trait ROVerificationRepository {

  def get(requester: UserID, request: VerificationID): Future[Option[ROVerificationRequest[Resource]]]

  def list(requester: UserID): Future[Set[ROVerificationRequest[Resource]]]

  def delete(requester: UserID, request: VerificationID): Future[Boolean]

  def save(req: ROVerificationRequest[Resource]): Future[ROVerificationRequest[Resource]]

  def update(req: ROVerificationRequest[Resource], status: ROVerificationRequestStatus): Future[Boolean]

}


