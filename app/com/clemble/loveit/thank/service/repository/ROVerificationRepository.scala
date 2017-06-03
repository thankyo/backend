package com.clemble.loveit.thank.service.repository

import com.clemble.loveit.common.model.{Resource, UserID}
import com.clemble.loveit.thank.model.{ROVerification, VerificationStatus}

import scala.concurrent.Future

trait ROVerificationRepository {

  def get(user: UserID, res: Resource): Future[Option[ROVerification[Resource]]]

  def list(user: UserID): Future[Set[ROVerification[Resource]]]

  def delete(user: UserID, res: Resource): Future[Boolean]

  def save(user: UserID, req: ROVerification[Resource]): Future[ROVerification[Resource]]

  def update(user: UserID, res: Resource, status: VerificationStatus): Future[Boolean]

}


