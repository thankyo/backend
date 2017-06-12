package com.clemble.loveit.payment.service.repository

import java.time.YearMonth

import akka.stream.scaladsl.Source
import com.clemble.loveit.common.error.RepositoryException
import com.clemble.loveit.common.model.UserID
import com.clemble.loveit.payment.model.EOMPayout
import com.clemble.loveit.payment.model.PayoutStatus.PayoutStatus
import com.clemble.loveit.user.service.repository.UserAwareRepository
import play.api.libs.json.JsValue

import scala.concurrent.Future

trait EOMPayoutRepository extends UserAwareRepository[EOMPayout] {

  /**
    * Lists all charges with specified status
    */
  def listPending(yom: YearMonth): Source[EOMPayout, _]


  /**
    * Updates charge status, specifying details and status for pending Charge,
    *
    * @throws RepositoryException update only of pending charges is allowed, others will cause error,
    *                             that must be addressed by the admin
    */
  @throws[RepositoryException]
  def updatePending(user: UserID, yom: YearMonth, status: PayoutStatus, details: JsValue): Future[Boolean]

  /**
    * Save [[EOMPayout]]
    */
  def save(payout: EOMPayout): Future[Boolean]

}
