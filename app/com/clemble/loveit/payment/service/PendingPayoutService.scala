package com.clemble.loveit.payment.service

import akka.stream.scaladsl.Source
import com.clemble.loveit.common.model.UserID
import com.clemble.loveit.payment.model.PayoutStatus.PayoutStatus
import com.clemble.loveit.payment.model.{Payout}
import com.clemble.loveit.user.service.repository.UserAwareRepository

import scala.concurrent.Future

/**
  * Abstraction over Payout repository,
  * There can only be a single pending processing operation
  */
trait PendingPayoutService extends UserAwareRepository[Payout] {

  def listPending(): Source[Payout, _]

  def updatePending(user: UserID, processed: Long, pending: Long, failed: Long): Future[Boolean]

  def updatePending(user: UserID, status: PayoutStatus): Future[Boolean]

}
