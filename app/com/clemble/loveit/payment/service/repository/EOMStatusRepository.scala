package com.clemble.loveit.payment.service.repository

import java.time.YearMonth

import com.clemble.loveit.payment.model.{EOMStatistics, EOMStatus}
import org.joda.time.DateTime

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
             finished: DateTime): Future[Boolean]

}
