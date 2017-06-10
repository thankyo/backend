package com.clemble.loveit.payment.service.repository

import com.clemble.loveit.payment.model.EOMPayout
import com.clemble.loveit.user.service.repository.UserAwareRepository

import scala.concurrent.Future

trait EOMPayoutRepository extends UserAwareRepository[EOMPayout] {

  def save(payout: EOMPayout): Future[Boolean]

}
