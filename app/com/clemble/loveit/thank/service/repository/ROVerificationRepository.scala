package com.clemble.loveit.thank.service.repository

import com.clemble.loveit.common.model.{Resource, UserID}
import com.clemble.loveit.thank.model.{ROVerification, VerificationStatus, VerificationID}

import scala.concurrent.Future

trait ROVerificationRepository {

  def get(requester: UserID, request: VerificationID): Future[Option[ROVerification[Resource]]]

  def list(requester: UserID): Future[Set[ROVerification[Resource]]]

  def delete(requester: UserID, request: VerificationID): Future[Boolean]

  def save(req: ROVerification[Resource]): Future[ROVerification[Resource]]

  def update(req: ROVerification[Resource], status: VerificationStatus): Future[Boolean]

}


