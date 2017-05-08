package com.clemble.loveit.thank.service

import com.clemble.loveit.thank.model.OwnershipRequest

import scala.concurrent.Future

/**
  * Ownership verification service
  */
trait OwnershipVerificationService {

  def verify(ownershipRequest: OwnershipRequest): Future[Boolean]

}
