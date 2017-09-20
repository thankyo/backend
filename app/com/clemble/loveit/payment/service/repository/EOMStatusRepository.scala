package com.clemble.loveit.payment.service.repository

import java.time.{LocalDateTime, YearMonth}

import com.clemble.loveit.payment.model.{EOMStatistics, EOMStatus}

import scala.concurrent.Future

trait EOMStatusRepository {

  /**
    * @return optional EOMStatus
    */
  def get(yom: YearMonth): Future[Option[EOMStatus]]

  /**
    * Saves EOM status, if there were no previous EOMStatus created
    */
  def save(status: EOMStatus): Future[EOMStatus]

  /**
    * Update fields all at once for EOMStatus
    */
  def update(yom: YearMonth,
             createCharges: EOMStatistics,
             applyCharges: EOMStatistics,
             createPayout: EOMStatistics,
             applyPayout: EOMStatistics,
             finished: LocalDateTime): Future[Boolean]

}
