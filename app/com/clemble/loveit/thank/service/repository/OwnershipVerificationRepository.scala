package com.clemble.loveit.thank.service.repository

import com.clemble.loveit.thank.model.{OwnershipVerificationRequest, OwnershipVerificationRequestStatus}

import scala.concurrent.Future

trait OwnershipVerificationRepository {

  def save(req: OwnershipVerificationRequest): Future[OwnershipVerificationRequest]

  def update(req: OwnershipVerificationRequest, status: OwnershipVerificationRequestStatus): Future[Boolean]

}


